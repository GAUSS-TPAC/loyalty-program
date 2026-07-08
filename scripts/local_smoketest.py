#!/usr/bin/env python3
"""Smoke test of the loyalty-program backend running locally in dev profile.

In `dev` profile, JWT auth is fully disabled (permitAll) and a fixed default
tenant is injected by DevTenantResolutionFilter, so no Authorization header
is needed. See CLAUDE.md / DevSecurityConfig / DevTenantResolutionFilter.

Flow: create a CREDIT_POINTS rule -> activate it -> fire a matching event
for a fresh member -> verify points were credited.

Usage: python3 scripts/local_smoketest.py [base_url]
"""
import json
import sys
import urllib.error
import urllib.request
import uuid
from datetime import datetime, timezone

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


def main():
    member_id = str(uuid.uuid4())
    print(f"Test member: {member_id}")

    rule = call(
        "POST",
        "/api/v1/admin/rules",
        payload={
            "name": "Smoke test - points sur achat",
            "description": "Credite 100 points sur tout evenement PURCHASE",
            "trigger": {"eventType": "PURCHASE", "filters": {}},
            "conditions": [
                {
                    "type": "CUMULATIVE_COUNT",
                    "operator": "GREATER_THAN_OR_EQUAL",
                    "thresholdValue": 0,
                    "counterKey": "smoketest_purchase_count",
                }
            ],
            "effects": [{"type": "CREDIT_POINTS", "params": {"amount": 100}}],
            "priority": 1,
        },
    )
    rule_id = rule["id"]
    print(f"[+] Regle creee: {rule_id} (status={rule['status']})")

    call("PATCH", f"/api/v1/admin/rules/{rule_id}/activate")
    print("[+] Regle activee")

    call(
        "POST",
        "/api/v1/events",
        payload={
            "eventType": "PURCHASE",
            "memberId": member_id,
            "occurredAt": datetime.now(timezone.utc).isoformat(),
            "payload": {"amount": 50},
        },
    )
    print("[+] Evenement PURCHASE envoye")

    points = call("GET", f"/api/v1/members/{member_id}/points")
    print(f"\n=== Points du membre: {points} ===")


if __name__ == "__main__":
    main()
