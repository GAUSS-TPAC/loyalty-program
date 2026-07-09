#!/usr/bin/env python3
"""Smoke test of the wallet module workflow (module `wallet`: balances, transactions).

In `dev` profile, JWT auth is fully disabled (permitAll) and a fixed default
tenant is injected by DevTenantResolutionFilter, so no Authorization header
is needed.

Flow: create wallet (auto-activated) -> credit -> check balance -> debit ->
check transaction history -> freeze -> unfreeze.

Usage: python3 scripts/smoketest_wallet.py [base_url]
"""
import json
import sys
import urllib.error
import urllib.request
import uuid
from decimal import Decimal

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8082"


def call(method, path, payload=None):
    headers = {"Accept": "application/json"}
    data = None
    if payload is not None:
        headers["Content-Type"] = "application/json"
        data = json.dumps(payload).encode()

    req = urllib.request.Request(f"{BASE_URL}{path}", data=data, headers=headers, method=method)
    print(f"\n--- {method} {path} ---")
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            body = resp.read().decode()
            status = resp.status
    except urllib.error.HTTPError as e:
        body = e.read().decode()
        print(f"[HTTP {e.code}] {body}")
        sys.exit(1)

    result = json.loads(body) if body else None
    print(f"[{status}] {json.dumps(result, indent=2, ensure_ascii=False)[:600]}")
    return result


def expect(condition, message):
    if not condition:
        print(f"[-] ECHEC: {message}")
        sys.exit(1)
    print(f"[+] OK: {message}")


def main():
    member_id = str(uuid.uuid4())
    print(f"Test member: {member_id}")
    base = f"/api/v1/members/{member_id}/wallet"

    wallet = call("POST", base, payload={"currencyCode": "XAF", "autoActivate": True})
    expect(wallet["status"] == "ACTIVE", f"wallet cree et actif (status={wallet['status']})")
    expect(Decimal(str(wallet["balance"])) == 0, "solde initial a 0")

    wallet = call(
        "POST",
        f"{base}/credit",
        payload={"amount": 1000, "source": "MANUAL_ADJUSTMENT", "referenceId": "smoketest-credit-1"},
    )
    expect(Decimal(str(wallet["balance"])) == 1000, f"credit applique (balance={wallet['balance']})")

    wallet = call("GET", base)
    expect(Decimal(str(wallet["balance"])) == 1000, "solde relu coherent apres credit")

    debit = call(
        "POST",
        f"{base}/debit",
        payload={"amount": 400, "description": "Smoke test purchase", "orderReference": "SMOKE-001"},
    )
    expect(not debit["otpRequired"], "pas de challenge OTP (pas de politique OTP configuree en dev)")
    expect(Decimal(str(debit["newBalance"])) == 600, f"debit applique (newBalance={debit['newBalance']})")

    transactions = call("GET", f"{base}/transactions?page=0&size=20")
    expect(len(transactions) >= 2, f"historique contient au moins credit+debit ({len(transactions)} entrees)")
    types_seen = {t["type"] for t in transactions}
    expect({"CREDIT", "DEBIT"}.issubset(types_seen), f"historique contient CREDIT et DEBIT ({types_seen})")

    wallet = call("POST", f"{base}/freeze", payload={"reason": "Smoke test freeze"})
    expect(wallet["status"] == "FROZEN", f"wallet gele (status={wallet['status']})")

    wallet = call("POST", f"{base}/unfreeze")
    expect(wallet["status"] == "ACTIVE", f"wallet degele (status={wallet['status']})")

    print("\n=== WALLET: WORKFLOW RESPECTE ===")


if __name__ == "__main__":
    main()
