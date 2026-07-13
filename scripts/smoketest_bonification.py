#!/usr/bin/env python3
"""Smoke test of the Bonification page workflow (manual points adjustment).

In `dev` profile, JWT auth is fully disabled (permitAll) and a fixed default
tenant is injected by DevTenantResolutionFilter, so no Authorization header
is needed.

The /portal/bonification admin page credits/debits a member's *internal*
loyalty points balance directly (PointsAccount), via
POST /api/v1/members/{memberId}/points/adjust. It no longer calls the
external BonusAPI partner (see domain/bonification/, still present but
unused by this page).

Flow: credit a fresh member -> debit part of it -> attempt to overdraw
(expect a clean 422, not a 500) -> validation errors on malformed requests.

Usage: python3 scripts/smoketest_bonification.py [base_url]
"""
import json
import sys
import urllib.error
import urllib.request
import uuid

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
        with urllib.request.urlopen(req, timeout=15) as resp:
            body = resp.read().decode()
            status = resp.status
    except urllib.error.HTTPError as e:
        body = e.read().decode()
        print(f"[HTTP {e.code}] {body}")
        return e.code, (json.loads(body) if body else None)

    result = json.loads(body) if body else None
    print(f"[{status}] {json.dumps(result, indent=2, ensure_ascii=False)[:600]}")
    return status, result


def expect(condition, message):
    if not condition:
        print(f"[-] ECHEC: {message}")
        sys.exit(1)
    print(f"[+] OK: {message}")


def adjust(member_id, amount, debit, reason):
    return call(
        "POST",
        f"/api/v1/members/{member_id}/points/adjust",
        payload={"amount": amount, "debit": debit, "reason": reason},
    )


def main():
    member_id = str(uuid.uuid4())
    print(f"Test member: {member_id}")

    # 1. Crédit initial
    code, account = adjust(member_id, 150, False, "Smoke test - credit")
    expect(code == 200, "credit accepte (HTTP 200)")
    expect(account["availablePoints"] == 150, f"solde apres credit = 150 (obtenu {account.get('availablePoints')})")
    expect(account["lifetimeEarned"] == 150, "lifetimeEarned mis a jour")

    # 2. Débit partiel
    code, account = adjust(member_id, 50, True, "Smoke test - debit")
    expect(code == 200, "debit accepte (HTTP 200)")
    expect(account["availablePoints"] == 100, f"solde apres debit = 100 (obtenu {account.get('availablePoints')})")
    expect(account["lifetimeSpent"] == 50, "lifetimeSpent mis a jour")

    # 3. Débit au-delà du solde disponible -> refus metier propre (422), pas un 500
    code, problem = adjust(member_id, 999, True, "Smoke test - overdraw")
    expect(code == 422, f"debit superieur au solde refuse en 422 (obtenu {code})")
    expect(problem.get("title") == "LOYALTY_RULE_VIOLATION", "code d'erreur LOYALTY_RULE_VIOLATION")

    # 4. Validation: motif manquant -> 400
    code, _ = call(
        "POST",
        f"/api/v1/members/{member_id}/points/adjust",
        payload={"amount": 10, "debit": False, "reason": ""},
    )
    expect(code == 400, f"motif vide rejete en validation (obtenu {code})")

    # 5. Validation: montant negatif -> 400
    code, _ = call(
        "POST",
        f"/api/v1/members/{member_id}/points/adjust",
        payload={"amount": -10, "debit": False, "reason": "Invalide"},
    )
    expect(code == 400, f"montant negatif rejete en validation (obtenu {code})")

    # 6. Solde final inchangé après les tentatives invalides
    code, account = call("GET", f"/api/v1/members/{member_id}/points")
    expect(code == 200, "lecture du solde final")
    expect(account["availablePoints"] == 100, "solde final coherent (les tentatives invalides n'ont rien modifie)")

    # 7. ID membre malforme (pas un UUID) -> 400 propre, pas un 500
    code, _ = call(
        "POST",
        "/api/v1/members/not-a-valid-uuid/points/adjust",
        payload={"amount": 10, "debit": False, "reason": "ID malforme"},
    )
    expect(code == 400, f"memberId malforme rejete en 400, pas en 500 (obtenu {code})")

    print("\n=== BONIFICATION: WORKFLOW RESPECTE (ajustement interne des points) ===")


if __name__ == "__main__":
    main()
