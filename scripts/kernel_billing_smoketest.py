#!/usr/bin/env python3
"""Smoke test of the Kernel Core billing workflow for the loyalty-program org.

Ensures the LOYALTY-PROGRAM organization, its commercial subscription, a test
product and a test customer third-party exist, then creates a Facture
Proforma and a Bon de Réception against them.

Config comes from environment variables (see scripts/.env.example). Load them
with `set -a; source scripts/.env; set +a` before running, or via a tool like
python-dotenv.
"""
import json
import os
import sys
import urllib.error
import urllib.request

BASE_URL = os.environ["KERNEL_BASE_URL"]
CLIENT_ID = os.environ["KERNEL_CLIENT_ID"]
API_KEY = os.environ["KERNEL_API_KEY"]
TENANT_ID = os.environ["KERNEL_TENANT_ID"]
PRINCIPAL = os.environ["KERNEL_USER_EMAIL"]
PASSWORD = os.environ["KERNEL_USER_PASSWORD"]

ORG_CODE = os.environ.get("KERNEL_ORG_CODE", "LOYALTY-PROGRAM")
ORG_DISPLAY_NAME = os.environ.get("KERNEL_ORG_DISPLAY_NAME", "Loyalty Program")
ORG_LEGAL_NAME = os.environ.get("KERNEL_ORG_LEGAL_NAME", "Loyalty Program SARL")
PLAN_CODE = os.environ.get("KERNEL_PLAN_CODE", "ENTERPRISE")
PRODUCT_SKU = os.environ.get("KERNEL_PRODUCT_SKU", "SKU-LOYALTY-01")
PRODUCT_NAME = os.environ.get("KERNEL_PRODUCT_NAME", "Produit Loyalty Program")
PRODUCT_PRICE = float(os.environ.get("KERNEL_PRODUCT_PRICE", "2500.0"))
CURRENCY = os.environ.get("KERNEL_CURRENCY", "XAF")
THIRD_PARTY_REF = os.environ.get("KERNEL_THIRD_PARTY_REF", "TP-LOYALTY-01")
THIRD_PARTY_NAME = os.environ.get("KERNEL_THIRD_PARTY_NAME", "Client Loyalty Program")


def call(method, path, token=None, org_id=None, payload=None):
    headers = {
        "Accept": "application/json",
        "X-Client-Id": CLIENT_ID,
        "X-Api-Key": API_KEY,
        "X-Tenant-Id": TENANT_ID,
    }
    if org_id:
        headers["X-Organization-Id"] = org_id
    if token:
        headers["Authorization"] = f"Bearer {token}"

    data = None
    if payload is not None:
        headers["Content-Type"] = "application/json"
        data = json.dumps(payload).encode()

    req = urllib.request.Request(f"{BASE_URL}{path}", data=data, headers=headers, method=method)
    print(f"\n--- {method} {path} ---")
    try:
        with urllib.request.urlopen(req) as resp:
            body = resp.read().decode()
    except urllib.error.HTTPError as e:
        body = e.read().decode()
        print(f"[HTTP {e.code}] {body}")
        return json.loads(body) if body else None

    result = json.loads(body) if body else None
    print(json.dumps(result, indent=2, ensure_ascii=False)[:800])
    return result


def require(resp, message):
    if not resp or not resp.get("success"):
        print(f"[-] {message}")
        sys.exit(1)
    return resp["data"]


def main():
    login = require(
        call("POST", "/api/auth/login", payload={"principal": PRINCIPAL, "password": PASSWORD}),
        "Echec de connexion",
    )
    token = login["accessToken"]
    actor_id = login["actorId"]

    actor = require(call("GET", "/api/actors/me", token=token), "Echec recuperation profil acteur")
    business_actor_id = actor["id"]

    orgs = require(call("GET", "/api/organizations/my", token=token), "Echec recuperation organisations")
    org = next((o for o in orgs if o.get("code") == ORG_CODE), None)
    if org is None:
        org = require(
            call(
                "POST",
                "/api/organizations",
                token=token,
                payload={
                    "businessActorId": business_actor_id,
                    "code": ORG_CODE,
                    "legalName": ORG_LEGAL_NAME,
                    "displayName": ORG_DISPLAY_NAME,
                    "organizationType": "PRIVATE_COMPANY",
                },
            ),
            "Echec creation organisation",
        )
    org_id = org["id"]
    print(f"[+] Organisation: {ORG_CODE} ({org_id})")

    call(
        "POST",
        f"/api/organizations/{org_id}/commercial-subscriptions",
        token=token,
        org_id=org_id,
        payload={"planCode": PLAN_CODE, "addOnCodes": []},
    )

    products = require(
        call("GET", f"/api/products?organizationId={org_id}", token=token, org_id=org_id),
        "Echec recuperation produits",
    )
    product = next((p for p in products if p.get("sku") == PRODUCT_SKU), None)
    if product is None:
        product = require(
            call(
                "POST",
                "/api/products",
                token=token,
                org_id=org_id,
                payload={
                    "organizationId": org_id,
                    "sku": PRODUCT_SKU,
                    "name": PRODUCT_NAME,
                    "familyCode": "DEFAULT",
                    "variantLabel": "Standard",
                    "unitPrice": PRODUCT_PRICE,
                    "currency": CURRENCY,
                },
            ),
            "Echec creation produit",
        )
    product_id = product["id"]
    print(f"[+] Produit: {PRODUCT_SKU} ({product_id})")

    third_party = require(
        call(
            "POST",
            f"/api/third-parties/actors/{actor_id}/financial-profile",
            token=token,
            org_id=org_id,
            payload={
                "organizationId": org_id,
                "role": "CUSTOMER",
                "referenceCode": THIRD_PARTY_REF,
                "displayName": THIRD_PARTY_NAME,
            },
        ),
        "Echec creation tiers",
    )
    third_party_id = third_party["id"]
    print(f"[+] Tiers: {THIRD_PARTY_REF} ({third_party_id})")

    call(
        "POST",
        "/api/factures-proforma",
        token=token,
        org_id=org_id,
        payload={
            "counterpartyThirdPartyId": third_party_id,
            "currency": CURRENCY,
            "lines": [{"productId": product_id, "quantity": 3.0, "unitPrice": PRODUCT_PRICE}],
        },
    )
    call("GET", "/api/factures-proforma", token=token, org_id=org_id)

    call(
        "POST",
        "/api/v1/facturation/bon-receptions",
        token=token,
        org_id=org_id,
        payload={
            "counterpartyThirdPartyId": third_party_id,
            "currency": CURRENCY,
            "lines": [{"productId": product_id, "quantity": 10.0, "unitPrice": 2000.0}],
        },
    )
    call("GET", "/api/v1/facturation/bon-receptions", token=token, org_id=org_id)

    print("\n=== TERMINE ===")


if __name__ == "__main__":
    main()
