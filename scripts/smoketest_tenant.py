#!/usr/bin/env python3
"""Smoke test of the tenant module workflow (multi-tenancy, API keys, health).

In `dev` profile, JWT auth is fully disabled (permitAll) and a fixed default
tenant is injected by DevTenantResolutionFilter, so no Authorization header
is needed.

Flow: check tenant health -> create API key -> list API keys -> revoke it.

Usage: python3 scripts/smoketest_tenant.py [base_url]
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
        with urllib.request.urlopen(req, timeout=10) as resp:
            body = resp.read().decode()
            status = resp.status
    except urllib.error.HTTPError as e:
        body = e.read().decode()
        print(f"[HTTP {e.code}] {body}")
        sys.exit(1)

    result = json.loads(body) if body else None
    print(f"[{status}] {json.dumps(result, indent=2, ensure_ascii=False)[:600]}")
    return status, result


def expect(condition, message):
    if not condition:
        print(f"[-] ECHEC: {message}")
        sys.exit(1)
    print(f"[+] OK: {message}")


def main():
    _, health = call("GET", "/api/health")
    expect(health.get("status") == "ACTIVE", f"tenant dev actif (status={health.get('status')})")
    expect(health.get("tenantId") is not None, "tenantId present dans la reponse")
    print(f"[i] tenantId={health.get('tenantId')} tenantName={health.get('tenantName')}")

    _, key = call("POST", "/api/v1/admin/api-keys", payload={"name": "Smoke test key", "mode": "TEST"})
    expect(key.get("rawKey"), "cle API brute retournee a la creation (visible une seule fois)")
    key_id = key["id"]
    print(f"[+] Cle creee: {key_id}")

    _, keys = call("GET", "/api/v1/admin/api-keys")
    listed = next((k for k in keys if k["id"] == key_id), None)
    expect(listed is not None, "la cle creee apparait dans la liste du tenant")
    expect(listed.get("rawKey") is None, "la cle brute n'est plus exposee en liste (masquee apres creation)")

    status, _ = call("DELETE", f"/api/v1/admin/api-keys/{key_id}")
    expect(status == 204, f"revocation de la cle reussie (HTTP {status})")

    print("\n=== TENANT: WORKFLOW RESPECTE ===")


if __name__ == "__main__":
    main()
