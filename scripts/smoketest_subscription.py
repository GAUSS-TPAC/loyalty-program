#!/usr/bin/env python3
"""Smoke test of the subscription module workflow (SaaS billing: plans, tenant
subscription, invoices). This is our own equivalent of a "billing" module --
unlike Kernel Core's commerce billing (organizations/products/third-parties/
proforma invoices), our subscription domain is simpler: a tenant subscribes to
one of our SubscriptionPlan records and gets InvoiceRecord rows generated
automatically.

In `dev` profile, JWT auth is fully disabled (permitAll) and a fixed default
tenant is injected by DevTenantResolutionFilter, so no Authorization header
is needed.

Note: the dev tenant is fixed (00000000-0000-0000-0000-000000000001), so
unlike scripts that create a fresh member/wallet every run, this one can't
blindly POST /subscriptions every time (the domain throws ALREADY_SUBSCRIBED,
HTTP 409, if the tenant already has a non-terminal subscription). It checks
for existing state first and only creates what's missing -- same idempotent
"reuse or create" pattern as test_billing.py uses against Kernel Core.

Flow: ensure a test plan exists -> ensure the tenant is subscribed (subscribe
if unsubscribed/terminal, reuse if already active/trial) -> verify an invoice
was generated for that subscription.

Usage: python3 scripts/smoketest_subscription.py [base_url]
"""
import json
import sys
import urllib.error
import urllib.request

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8082"

PLAN_CODE = "SMOKETEST_PLAN"


def call(method, path, payload=None, allow_statuses=None):
    """allow_statuses: HTTP error codes treated as expected outcomes instead of
    hard failures; returned as (status, body) so the caller can branch on them."""
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
        if allow_statuses and e.code in allow_statuses:
            print(f"[HTTP {e.code}] {body[:300]} (attendu)")
            return e.code, (json.loads(body) if body else None)
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


def ensure_test_plan():
    _, plans = call("GET", "/api/v1/subscription-plans")
    plan = next((p for p in plans if p["code"] == PLAN_CODE), None)
    if plan is not None:
        print(f"[+] Plan de test existant reutilise : {plan['name']} (id={plan['id']})")
        return plan

    print("[!] Plan de test introuvable, creation...")
    _, plan = call("POST", "/api/v1/subscription-plans", payload={
        "code": PLAN_CODE,
        "name": "Smoke Test Plan",
        "description": "Plan cree par scripts/smoketest_subscription.py",
        "priceMonthly": 5000,
        "priceYearly": 50000,
        "currency": "XAF",
        "maxRules": 10,
        "maxMembers": 1000,
        "maxEventsPerMonth": 10000,
        "referralEnabled": True,
        "campaignsEnabled": True,
        "promoCodesEnabled": True,
        "analyticsEnabled": True,
    })
    print(f"[+] Plan de test cree : id={plan['id']}")
    return plan


def ensure_active_subscription(plan_id):
    status, sub = call("GET", "/api/v1/subscriptions/me", allow_statuses={404})

    if status == 404 or sub.get("status") in ("CANCELLED", "EXPIRED"):
        reason = "aucun abonnement" if status == 404 else f"abonnement terminal (status={sub.get('status')})"
        print(f"[!] {reason} -> souscription au plan de test...")
        _, sub = call("POST", "/api/v1/subscriptions", payload={
            "planId": plan_id,
            "billingCycle": "MONTHLY",
        })

    return sub


def main():
    plan = ensure_test_plan()
    expect(plan.get("active") is True, f"plan actif (code={plan['code']})")

    sub = ensure_active_subscription(plan["id"])
    expect(sub.get("status") in ("ACTIVE", "TRIAL"), f"abonnement du tenant actif (status={sub.get('status')})")
    expect(sub.get("tenantId") is not None, "tenantId present dans l'abonnement")
    sub_id = sub["id"]
    print(f"[i] subscriptionId={sub_id} planId={sub.get('planId')} cycle={sub.get('billingCycle')}")

    _, invoices = call("GET", "/api/v1/subscriptions/me/invoices")
    expect(isinstance(invoices, list), "liste de factures recuperee")
    linked = [i for i in invoices if i.get("subscriptionId") == sub_id]
    expect(len(linked) >= 1,
           f"au moins une facture generee pour cet abonnement ({len(invoices)} facture(s) au total sur le tenant)")
    print(f"[i] derniere facture: montant={linked[0].get('amount')} {linked[0].get('currency')} "
          f"status={linked[0].get('status')}")

    print("\n=== SUBSCRIPTION: WORKFLOW RESPECTE ===")


if __name__ == "__main__":
    main()
