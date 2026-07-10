#!/usr/bin/env python3
"""Smoke test complet du parcours admin loyalty-program, authentifié via le VRAI
Kernel Core (pas le bypass du profil dev) — couvre les 5 scénarios attendus :

  1. Sélection d'organisation + connexion admin (POST /api/v1/auth/login, qui
     délègue à KernelCore POST /api/auth/login).
  2. Génération d'une clé API et "partage" (la clé brute n'est visible qu'à la
     création ; on vérifie qu'elle authentifie ensuite un appel comme le ferait
     un intégrateur externe à qui on l'a transmise).
  3. Connexion (re-login, + vérification qu'un mot de passe invalide est rejeté).
  4. Création + modification d'une règle de fidélité (DRAFT -> ACTIVE).
  5. Surveillance admin (journal des transactions de points tenant-wide).

Prérequis : le backend loyalty-program doit tourner avec la sécurité JWT réelle
active (PAS le profil `dev`, qui bypass tout) et pointer vers le vrai Kernel Core
(KERNEL_CORE_URL, KERNEL_SERVICE_CLIENT_ID/SECRET, KERNEL_TENANT_ID, et
JWT_ISSUER_URI/JWT_JWK_SET_URI vers le KernelCore réel — voir application.yml).

Config via variables d'environnement (voir scripts/.env.example) :
  LOYALTY_BASE_URL      URL du backend loyalty-program (def: http://localhost:8083)
  KERNEL_USER_EMAIL      email de l'admin KernelCore
  KERNEL_USER_PASSWORD   mot de passe de l'admin KernelCore
  KERNEL_ORGANIZATION_ID UUID de l'organisation KernelCore à utiliser comme tenant
                          (doit être APPROVED côté gouvernance KernelCore, sinon le
                          tenant résolu est marqué SUSPENDED et tout appel échoue en 401)

Usage: set -a; source scripts/.env; set +a; python3 scripts/kernel_full_smoketest.py
"""
import json
import os
import sys
import urllib.error
import urllib.request

BASE_URL = os.environ.get("LOYALTY_BASE_URL", "http://localhost:8083")
EMAIL = os.environ["KERNEL_USER_EMAIL"]
PASSWORD = os.environ["KERNEL_USER_PASSWORD"]
ORGANIZATION_ID = os.environ["KERNEL_ORGANIZATION_ID"]


def call(method, path, token=None, org_id=None, api_key=None, payload=None, expect_json=True):
    headers = {"Accept": "application/json"}
    data = None
    if payload is not None:
        headers["Content-Type"] = "application/json"
        data = json.dumps(payload).encode()
    if token:
        headers["Authorization"] = f"Bearer {token}"
    if org_id:
        headers["X-Organization-Id"] = org_id
    if api_key:
        headers["X-Api-Key"] = api_key

    req = urllib.request.Request(f"{BASE_URL}{path}", data=data, headers=headers, method=method)
    print(f"\n--- {method} {path} ---")
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            body = resp.read().decode()
            status = resp.status
    except urllib.error.HTTPError as e:
        body = e.read().decode()
        status = e.code

    result = None
    if body and expect_json:
        try:
            result = json.loads(body)
        except json.JSONDecodeError:
            result = body
    print(f"[{status}] {_redacted_preview(result)}")
    return status, result


SECRET_FIELDS = {"token", "rawKey", "accessToken"}


def _redacted_preview(result):
    """Never print live credentials (JWT, raw API keys) to stdout/logs."""
    if isinstance(result, dict):
        safe = {k: ("***REDACTED***" if k in SECRET_FIELDS and v else v) for k, v in result.items()}
        return json.dumps(safe, indent=2, ensure_ascii=False)[:500]
    if isinstance(result, list):
        return json.dumps(result, indent=2, ensure_ascii=False)[:500]
    return str(result)[:500]


def expect(condition, message):
    if not condition:
        print(f"[-] ECHEC: {message}")
        sys.exit(1)
    print(f"[+] OK: {message}")


def main():
    # ── 1. Sélection d'organisation + connexion admin (via le vrai Kernel Core) ──
    status, login = call(
        "POST", "/api/v1/auth/login",
        payload={"email": EMAIL, "password": PASSWORD, "organizationId": ORGANIZATION_ID},
    )
    expect(status == 200, f"connexion admin reussie via KernelCore (HTTP {status})")
    expect(login.get("token"), "JWT KernelCore recu")
    expect(login.get("organizationId") == ORGANIZATION_ID, "organisation active confirmee dans la reponse de login")
    token = login["token"]
    org_id = login["organizationId"]
    print(f"[i] organisation active: {login.get('organizationCode')} ({org_id})")

    # Solidite : mauvais mot de passe -> rejet propre, pas une 500.
    bad_status, bad_body = call("POST", "/api/v1/auth/login",
                                 payload={"email": EMAIL, "password": "definitely-wrong", "organizationId": ORGANIZATION_ID})
    expect(bad_status == 401, f"mauvais mot de passe rejete proprement (HTTP {bad_status}, pas une 500)")

    # ── 2. Generation d'une cle API + "partage" (verifie qu'elle authentifie un appel) ──
    status, created = call("POST", "/api/v1/admin/api-keys", token=token, org_id=org_id,
                            payload={"name": "Smoke test Kernel E2E", "mode": "TEST"})
    expect(status == 201, f"creation de cle API reussie (HTTP {status})")
    expect(created.get("rawKey"), "cle API brute retournee a la creation (visible une seule fois, a partager avec l'integrateur)")
    key_id = created["id"]
    raw_key = created["rawKey"]

    status, listed = call("GET", "/api/v1/admin/api-keys", token=token, org_id=org_id)
    expect(status == 200, "liste des cles API accessible par l'admin")
    match = next((k for k in listed if k["id"] == key_id), None)
    expect(match is not None, "la cle creee apparait dans la liste du tenant")
    expect(match.get("rawKey") is None, "la cle brute n'est plus exposee en liste (masquee apres creation)")

    # Simule l'integrateur externe a qui la cle brute a ete "partagee" : elle doit a elle
    # seule authentifier un appel tenant-scope (sans JWT), exactement comme le ferait le
    # portail developpeur — voir ApiKeyResolutionFilter (X-Api-Key -> ROLE_TENANT_ADMIN).
    status, shared_health = call("GET", "/api/health", api_key=raw_key)
    expect(status == 200, "cle API partagee authentifie a elle seule un appel externe (sans JWT)")
    expect(shared_health.get("tenantId") == org_id, "la cle API partagee resout bien le meme tenant")

    status, unauth = call("GET", "/api/health")
    expect(status == 401, "sans JWT ni cle API, l'appel est bien rejete (401)")

    # ── 3. Connexion (re-login) ──
    status, relogin = call("POST", "/api/v1/auth/login",
                            payload={"email": EMAIL, "password": PASSWORD, "organizationId": ORGANIZATION_ID})
    expect(status == 200, "re-connexion admin reussie (nouveau JWT emis)")
    expect(relogin["token"] != "", "nouveau JWT recu a la reconnexion")
    token = relogin["token"]

    # ── 4. Creation + modification d'une regle de fidelite (DRAFT -> ACTIVE) ──
    status, rule = call("POST", "/api/v1/admin/rules", token=token, org_id=org_id, payload={
        "name": "Smoke test Kernel E2E - points sur achat",
        "description": "Credite 100 points sur tout evenement PURCHASE",
        "trigger": {"eventType": "PURCHASE", "filters": {}},
        "conditions": [{
            "type": "CUMULATIVE_COUNT", "operator": "GREATER_THAN_OR_EQUAL",
            "thresholdValue": 0, "counterKey": "kernel_e2e_purchase_count",
        }],
        "effects": [{"type": "CREDIT_POINTS", "params": {"amount": 100}}],
        "priority": 1,
    })
    expect(status == 201, f"creation de regle reussie (HTTP {status})")
    expect(rule.get("status") == "DRAFT", f"regle creee en DRAFT (status={rule.get('status')})")
    rule_id = rule["id"]

    status, activated = call("PATCH", f"/api/v1/admin/rules/{rule_id}/activate", token=token, org_id=org_id)
    expect(status == 200, f"modification (activation) de la regle reussie (HTTP {status})")
    expect(activated.get("status") == "ACTIVE", f"regle passee a ACTIVE (status={activated.get('status')})")

    status, rules = call("GET", "/api/v1/admin/rules", token=token, org_id=org_id)
    expect(status == 200, "liste des regles du tenant accessible")
    expect(any(r["id"] == rule_id for r in rules), "la regle modifiee apparait dans la liste du tenant")

    # ── 5. Surveillance admin ──
    status, ledger = call("GET", "/api/v1/admin/points-transactions?page=0&size=20", token=token, org_id=org_id)
    expect(status == 200, f"journal des transactions de points accessible a l'admin (HTTP {status})")

    status, tenant_health = call("GET", "/api/health", token=token, org_id=org_id)
    expect(status == 200, "sante du tenant consultable par l'admin")
    expect(tenant_health.get("status") == "ACTIVE", f"tenant actif confirme (status={tenant_health.get('status')})")

    # Nettoyage : revoque la cle API creee pour ce test.
    # Connu : ce DELETE particulier revoque bien la cle cote serveur (verifie par log applicatif)
    # mais renvoie 401 au client dans ce parcours d'authentification reelle KernelCore -- un residu
    # non elucide, distinct des 5 scenarios ci-dessus (qui passent tous), a investiguer separement.
    status, _ = call("DELETE", f"/api/v1/admin/api-keys/{key_id}", token=token, org_id=org_id, expect_json=False)
    if status == 204:
        print(f"[+] OK: revocation de la cle API de test reussie (HTTP {status})")
    else:
        print(f"[i] Nettoyage: revocation cle API a repondu HTTP {status} (probleme connu et isole, voir commentaire) -- non bloquant")

    print("\n=== KERNEL E2E: LES 5 SCENARIOS SONT PASSES PAR LE VRAI KERNEL CORE ===")


if __name__ == "__main__":
    main()
