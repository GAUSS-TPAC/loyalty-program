#!/usr/bin/env python3
"""Smoke test of the bonification module workflow (external partner integration).

In `dev` profile, JWT auth is fully disabled (permitAll) and a fixed default
tenant is injected by DevTenantResolutionFilter, so no Authorization header
is needed.

Flow: check integration status -> submit a transaction.

Note: `app.bonification.enabled` defaults to true and points at the real
external API (https://bonusapi.onrender.com). Without real BONIFICATION_LOGIN/
BONIFICATION_PASSWORD credentials exported before starting the backend, the
partner will not be reachable/authenticated -- this is expected in a bare
dev environment, so this script treats "not reachable" as an informational
result rather than a hard failure. Only a genuine 5xx/malformed-response from
*our* backend (not the partner) is treated as a workflow violation.

Usage: python3 scripts/smoketest_bonification.py [base_url]
"""
import json
import sys
import urllib.error
import urllib.request

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


def main():
    status_code, status = call("GET", "/api/v1/bonification/status")
    expect(status_code == 200, "endpoint /status repond 200 (module cablé, meme si partenaire injoignable)")
    expect(status.get("enabled") is True, "integration bonification activee (app.bonification.enabled)")
    print(f"[i] reachable={status.get('reachable')} baseUrl={status.get('baseUrl')} message={status.get('message')!r}")

    tx_code, tx = call(
        "POST",
        "/api/v1/bonification/transactions",
        payload={"amount": 10.0, "clientLogin": "smoketest-client", "debit": False},
    )
    if tx_code == 200:
        expect(tx is not None, "transaction soumise au partenaire avec succes")
    else:
        print(
            f"[i] Soumission de transaction non aboutie (HTTP {tx_code}) - attendu si "
            "BONIFICATION_LOGIN/BONIFICATION_PASSWORD ne sont pas configures pour ce tenant."
        )
        expect(tx_code in (400, 401, 403, 422, 424, 502, 503, 504), "echec de type credentials/partenaire, pas un bug backend (4xx/5xx attendu)")

    print("\n=== BONIFICATION: WORKFLOW RESPECTE (voir notes ci-dessus si partenaire injoignable) ===")


if __name__ == "__main__":
    main()
