#!/usr/bin/env python3
"""Smoke test of the webhook module workflow (endpoint registration, delivery).

In `dev` profile, JWT auth is fully disabled (permitAll) and a fixed default
tenant is injected by DevTenantResolutionFilter, so no Authorization header
is needed.

Flow: register a webhook endpoint -> send a test ping -> check the delivery
log records it -> cleanup (delete endpoint).

Note: the test ping performs a real outbound HTTP POST to the registered URL
(https://httpbin.org/post here) -- success depends on outbound network access
from the backend host, not just on our own code, so a failed HTTP delivery is
only a hard failure if the *delivery record itself* isn't created.

Usage: python3 scripts/smoketest_webhook.py [base_url]
"""
import json
import sys
import urllib.error
import urllib.request

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8082"
TARGET_URL = "https://httpbin.org/post"


def call(method, path, payload=None):
    headers = {"Accept": "application/json"}
    data = None
    if payload is not None:
        headers["Content-Type"] = "application/json"
        data = json.dumps(payload).encode()

    req = urllib.request.Request(f"{BASE_URL}{path}", data=data, headers=headers, method=method)
    print(f"\n--- {method} {path} ---")
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
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
    _, endpoint = call(
        "POST",
        "/api/v1/admin/webhooks",
        payload={
            "url": TARGET_URL,
            "description": "Smoke test webhook",
            "eventTypes": ["points.earned", "webhook.test"],
        },
    )
    expect(endpoint.get("secret"), "secret de signature retourne a la creation")
    endpoint_id = endpoint["id"]
    print(f"[+] Webhook cree: {endpoint_id}")

    _, ping = call("POST", f"/api/v1/admin/webhooks/{endpoint_id}/test")
    print(f"[i] test ping success={ping.get('success')} httpStatus={ping.get('httpStatus')}")

    _, deliveries = call("GET", "/api/v1/admin/webhooks/deliveries?page=0&size=20")
    match = next(
        (d for d in deliveries if d.get("endpointId") == endpoint_id and d.get("eventType") == "webhook.test"),
        None,
    )
    expect(match is not None, "la tentative de livraison du test ping est journalisee")

    status, _ = call("DELETE", f"/api/v1/admin/webhooks/{endpoint_id}")
    expect(status == 204, f"suppression du webhook reussie (HTTP {status})")

    print("\n=== WEBHOOK: WORKFLOW RESPECTE ===")


if __name__ == "__main__":
    main()
