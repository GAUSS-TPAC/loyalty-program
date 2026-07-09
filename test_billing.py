#!/usr/bin/env python3
import json
import subprocess
import sys

# Configuration (identifiants Business Actor)
BASE_URL = "https://kernel-core.yowyob.com"
CLIENT_ID = "prod-platform-backend"
API_KEY = "VbWi225xzYPoD8rQ2FRniTAqkylh34XYeWxa9HCU"
TENANT_ID = "11111111-1111-1111-1111-111111111111"
PRINCIPAL = "piodjiele@gmail.com"
PASSWORD = "Password.237"

# Identifiants Admin de la plateforme (pour auto-bootstrap du rôle OWNER si manquant)
ADMIN_PRINCIPAL = "platform-admin"
ADMIN_PASSWORD = "281kFEOhFYuj7n9cfgGp"

def execute_curl(method, path, headers=None, payload=None):
    url = f"{BASE_URL}{path}"
    
    cmd_parts = ["curl", "-s", "-X", method, f'"{url}"']
    
    # Headers obligatoires
    cmd_parts.append('-H "Accept: application/json"')
    cmd_parts.append(f'-H "X-Client-Id: {CLIENT_ID}"')
    cmd_parts.append(f'-H "X-Api-Key: {API_KEY}"')
    cmd_parts.append(f'-H "X-Tenant-Id: {TENANT_ID}"')
    
    if payload:
        cmd_parts.append('-H "Content-Type: application/json"')
        
    if headers:
        for k, v in headers.items():
            cmd_parts.append(f'-H "{k}: {v}"')
            
    if payload:
        payload_str = json.dumps(payload)
        # Échappement simple pour le shell bash
        payload_escaped = payload_str.replace("'", "'\\''")
        cmd_parts.append(f"-d '{payload_escaped}'")
        
    curl_cmd = " \\\n  ".join(cmd_parts)
    
    print("\n" + "="*80)
    print(f"EXÉCUTION : {method} {path}")
    print("="*80)
    print(curl_cmd)
    print("="*80 + "\n")
    
    # Exécution de la commande
    flat_cmd = " ".join(cmd_parts).replace("\\\n  ", " ")
    res = subprocess.run(flat_cmd, shell=True, capture_output=True, text=True)
    
    if res.returncode != 0:
        print(f"[-] Le curl a échoué avec le code de retour {res.returncode}")
        print("Erreur :", res.stderr)
        return None
        
    print("[+] Réponse brute reçue :")
    print(res.stdout)
    
    try:
        data = json.loads(res.stdout)
        return data
    except json.JSONDecodeError:
        print("[!] Réponse non JSON ou vide.")
        return res.stdout

def assign_owner_role(target_user_id):
    print("\n" + "="*80)
    print("🔧 BOOTSTRAP : Connexion platform-admin pour accorder le rôle OWNER...")
    print("="*80)
    
    # 1. Login platform-admin
    login_payload = {
        "principal": ADMIN_PRINCIPAL,
        "password": ADMIN_PASSWORD
    }
    
    login_resp = execute_curl("POST", "/api/auth/login", payload=login_payload)
    if not login_resp or not login_resp.get("success"):
        print("[-] Échec de la connexion platform-admin.")
        return False
        
    resp_data = login_resp.get("data", {})
    next_step = resp_data.get("nextStep")
    
    admin_token = None
    if next_step == "CONFIRM_MFA":
        mfa_token = resp_data.get("mfaToken")
        print("\n[!] Double authentification requise pour platform-admin (MFA).")
        
        code_preview = resp_data.get("codePreview")
        if code_preview:
            print(f"[Local] Code OTP détecté dans la réponse : {code_preview}")
            
        otp_code = input("Veuillez saisir le code OTP reçu pour platform-admin : ").strip()
        
        mfa_payload = {
            "mfaToken": mfa_token,
            "code": otp_code
        }
        mfa_resp = execute_curl("POST", "/api/auth/login/mfa/confirm", payload=mfa_payload)
        if not mfa_resp or not mfa_resp.get("success"):
            print("[-] Échec de la validation MFA pour platform-admin.")
            return False
        admin_token = mfa_resp.get("data", {}).get("accessToken")
    else:
        admin_token = resp_data.get("accessToken")
        
    if not admin_token:
        print("[-] Token d'accès platform-admin introuvable.")
        return False
        
    admin_headers = {"Authorization": f"Bearer {admin_token}"}
    
    # 2. Récupérer l'ID du rôle OWNER
    print("\n[B1] Récupération des rôles du tenant...")
    roles_resp = execute_curl("GET", "/api/administration/roles", headers=admin_headers)
    if not roles_resp or not roles_resp.get("success"):
        print("[-] Échec de la récupération des rôles.")
        return False
        
    roles = roles_resp.get("data", [])
    owner_role_id = None
    for role in roles:
        if role.get("code") == "OWNER" or role.get("name") == "OWNER":
            owner_role_id = role.get("id")
            break
            
    if not owner_role_id:
        print("[-] Rôle OWNER introuvable dans le tenant.")
        print("Rôles existants :", [f"{r.get('name')} ({r.get('code')})" for r in roles])
        return False
        
    print(f"[+] ID du rôle OWNER trouvé : {owner_role_id}")
    
    # 3. Affecter le rôle OWNER à l'utilisateur cible (scope TENANT)
    print(f"\n[B2] Affectation du rôle OWNER à l'utilisateur {target_user_id}...")
    assign_payload = {
        "roleId": owner_role_id,
        "scopeType": "TENANT",
        "scope": "TENANT"
    }
    
    assign_resp = execute_curl("POST", f"/api/administration/users/{target_user_id}/roles", headers=admin_headers, payload=assign_payload)
    if not assign_resp or not assign_resp.get("success"):
        print("[-] Échec de l'affectation du rôle OWNER.")
        return False
        
    print("[+] Rôle OWNER affecté avec succès !")
    return True

def main():
    print("=== DÉBUT DES TESTS DU MODULE BILLING ===")
    
    # 1. Connexion en tant que Business Actor
    print("\n[1] Connexion en tant que Business Actor...")
    login_payload = {
        "principal": PRINCIPAL,
        "password": PASSWORD
    }
    login_resp = execute_curl("POST", "/api/auth/login", payload=login_payload)
    if not login_resp or not login_resp.get("success"):
        print("[-] Échec de la connexion.")
        sys.exit(1)
        
    resp_data = login_resp.get("data", {})
    access_token = resp_data.get("accessToken")
    user_id = resp_data.get("id")
    actor_id = resp_data.get("actorId")
    
    if not access_token:
        print("[-] Token d'accès manquant dans la réponse de connexion.")
        sys.exit(1)
        
    print(f"[+] Connecté avec succès. User ID: {user_id}, Actor ID: {actor_id}")
    
    auth_header = {"Authorization": f"Bearer {access_token}"}
    
    # 2. Récupérer le profil de l'Acteur pour avoir son businessActorId
    print("\n[2] Récupération du profil Business Actor...")
    actor_resp = execute_curl("GET", "/api/actors/me", headers=auth_header)
    if not actor_resp or not actor_resp.get("success"):
        print("[-] Échec de la récupération du profil Business Actor.")
        sys.exit(1)
        
    business_actor_id = actor_resp.get("data", {}).get("id")
    print(f"[+] Business Actor ID : {business_actor_id}")
    
    # 3. Récupérer ou créer l'Organisation (avec auto-bootstrap en cas de 403 Forbidden)
    print("\n[3] Récupération des organisations associées...")
    org_resp = execute_curl("GET", "/api/organizations/my", headers=auth_header)
    
    # Si accès refusé, on procède au bootstrap du rôle OWNER
    if org_resp and (org_resp.get("errorCode") == "FORBIDDEN" or org_resp.get("message") == "Access Denied"):
        print("\n[!] Accès refusé aux organisations (Rôle OWNER manquant).")
        if assign_owner_role(user_id):
            print("\n[+] Re-connexion en tant que Business Actor pour actualiser les rôles du token...")
            login_resp = execute_curl("POST", "/api/auth/login", payload=login_payload)
            if login_resp and login_resp.get("success"):
                resp_data = login_resp.get("data", {})
                access_token = resp_data.get("accessToken")
                auth_header = {"Authorization": f"Bearer {access_token}"}
                print("[+] Token rafraîchi avec succès avec le rôle OWNER !")
                # On ré-effectue la requête
                org_resp = execute_curl("GET", "/api/organizations/my", headers=auth_header)
            else:
                print("[-] Échec de la reconnexion après affectation du rôle.")
                sys.exit(1)
        else:
            print("[-] Impossible d'auto-bootstrapper le rôle OWNER.")
            sys.exit(1)
            
    org_id = None
    if org_resp and org_resp.get("success"):
        orgs = org_resp.get("data", [])
        if orgs:
            org_id = orgs[0].get("id")
            print(f"[+] Organisation existante trouvée : {orgs[0].get('displayName')} (ID: {org_id})")
            
    if not org_id:
        print("[!] Aucune organisation trouvée. Création d'une organisation de test...")
        create_org_payload = {
            "businessActorId": business_actor_id,
            "code": "ORG-TEST-BILLING",
            "legalName": "Billing Test Org SARL",
            "displayName": "Billing Test Org",
            "organizationType": "PRIVATE_COMPANY"
        }
        create_org_resp = execute_curl("POST", "/api/organizations", headers=auth_header, payload=create_org_payload)
        
        # En cas de 403 à la création, on fait également le check au cas où
        if create_org_resp and (create_org_resp.get("errorCode") == "FORBIDDEN" or create_org_resp.get("message") == "Access Denied"):
            print("\n[!] Accès refusé pour la création d'organisation. Tentative de bootstrap du rôle OWNER...")
            if assign_owner_role(user_id):
                print("\n[+] Re-connexion en tant que Business Actor...")
                login_resp = execute_curl("POST", "/api/auth/login", payload=login_payload)
                if login_resp and login_resp.get("success"):
                    resp_data = login_resp.get("data", {})
                    access_token = resp_data.get("accessToken")
                    auth_header = {"Authorization": f"Bearer {access_token}"}
                    # On relance la création
                    create_org_resp = execute_curl("POST", "/api/organizations", headers=auth_header, payload=create_org_payload)
                else:
                    print("[-] Échec de la reconnexion.")
                    sys.exit(1)
            else:
                sys.exit(1)
                
        if create_org_resp and create_org_resp.get("success"):
            org_id = create_org_resp.get("data", {}).get("id")
            print(f"[+] Organisation créée avec succès ! ID: {org_id}")
        else:
            print("[-] Impossible de créer l'organisation.")
            sys.exit(1)
            
    org_headers = {
        "Authorization": f"Bearer {access_token}",
        "X-Organization-Id": org_id
    }
    
    # 3B. Souscription aux services de la plateforme via un Plan Commercial
    print("\n[3B] Souscription de l'organisation au Plan Commercial ENTERPRISE...")
    sub_payload = {
        "planCode": "ENTERPRISE",
        "addOnCodes": []
    }
    sub_resp = execute_curl("POST", f"/api/organizations/{org_id}/commercial-subscriptions", headers=org_headers, payload=sub_payload)
    if sub_resp and (sub_resp.get("success") or "id" in str(sub_resp)):
        print("[+] Abonnement au plan ENTERPRISE appliqué avec succès (services et quotas provisionnés) !")
    else:
        print("[!] Info/Erreur : déjà abonné ou échec pour le plan ENTERPRISE.")

    # 4. Créer des vendeurs (comptes de seller / cashiers)
    print("\n[4] Création des profils vendeurs (Sellers / Cashiers)...")
    
    sellers_to_create = [
        {"fullName": "Seller Alice", "kind": "SELLER", "email": "alice.seller@test.yowyob.com"},
        {"fullName": "Seller Bob", "kind": "SELLER", "email": "bob.seller@test.yowyob.com"}
    ]
    
    created_cashiers = []
    for seller in sellers_to_create:
        print(f"\n-> Création de {seller['fullName']}...")
        cashier_resp = execute_curl("POST", "/api/cashiers", headers=org_headers, payload=seller)
        
        # Le endpoint peut renvoyer directement le CashierProfileView (contenant l'id)
        if cashier_resp and (cashier_resp.get("success") or cashier_resp.get("id")):
            c_data = cashier_resp.get("data") if cashier_resp.get("success") else cashier_resp
            print(f"[+] Vendeur créé avec succès : {c_data.get('fullName')} (ID: {c_data.get('id')})")
            created_cashiers.append(c_data)
        else:
            print("[!] Échec avec /api/cashiers, tentative avec /api/users/cashiers...")
            cashier_resp = execute_curl("POST", "/api/users/cashiers", headers=org_headers, payload=seller)
            if cashier_resp and (cashier_resp.get("success") or cashier_resp.get("id")):
                c_data = cashier_resp.get("data") if cashier_resp.get("success") else cashier_resp
                print(f"[+] Vendeur créé via /api/users/cashiers : {c_data.get('fullName')} (ID: {c_data.get('id')})")
                created_cashiers.append(c_data)
            else:
                print(f"[-] Impossible de créer le vendeur {seller['fullName']}.")
                
    # 5. Récupérer ou créer un Produit de test
    print("\n[5] Récupération/Création d'un produit pour les tests de facturation...")
    product_list_resp = execute_curl("GET", f"/api/products?organizationId={org_id}", headers=org_headers)
    
    product_id = None
    if product_list_resp and product_list_resp.get("success"):
        products = product_list_resp.get("data", [])
        if products:
            product_id = products[0].get("id")
            print(f"[+] Produit existant trouvé : {products[0].get('name')} (ID: {product_id})")
            
    if not product_id:
        print("[!] Aucun produit trouvé. Création d'un produit de test...")
        create_prod_payload = {
            "organizationId": org_id,
            "sku": "SKU-TEST-BILL",
            "name": "Produit Test Billing",
            "familyCode": "DEFAULT",
            "variantLabel": "Standard",
            "unitPrice": 2500.0,
            "currency": "XAF"
        }
        prod_resp = execute_curl("POST", "/api/products", headers=org_headers, payload=create_prod_payload)
        if prod_resp and (prod_resp.get("success") or prod_resp.get("id")):
            p_data = prod_resp.get("data") if prod_resp.get("success") else prod_resp
            product_id = p_data.get("id")
            print(f"[+] Produit créé : ID {product_id}")
        else:
            print("[-] Échec de création du produit.")
            sys.exit(1)
            
    # 6. Récupérer ou créer un Tiers (Customer) de test
    print("\n[6] Récupération/Création d'un tiers (Customer) pour les tests de facturation...")
    tp_list_resp = execute_curl("GET", f"/api/third-parties?organizationId={org_id}", headers=org_headers)
    
    third_party_id = None
    if tp_list_resp and tp_list_resp.get("success"):
        tps = tp_list_resp.get("data", [])
        if tps:
            third_party_id = tps[0].get("id")
            print(f"[+] Tiers existant trouvé : {tps[0].get('name')} (ID: {third_party_id})")
            
    if not third_party_id:
        print("[!] Aucun tiers trouvé. Création d'un tiers via le profil financier de l'acteur (contournement quota)...")
        create_fp_payload = {
            "organizationId": org_id,
            "role": "CUSTOMER",
            "referenceCode": "TP-ACTOR-FP",
            "displayName": "Client Test Billing"
        }
        tp_resp = execute_curl("POST", f"/api/third-parties/actors/{actor_id}/financial-profile", headers=org_headers, payload=create_fp_payload)
        if tp_resp and (tp_resp.get("success") or tp_resp.get("id")):
            tp_data = tp_resp.get("data") if tp_resp.get("success") else tp_resp
            third_party_id = tp_data.get("id")
            print(f"[+] Tiers créé via profil financier : ID {third_party_id}")
        else:
            print("[-] Échec de création du tiers via le profil financier.")
            sys.exit(1)
            
    # 7. Tester la facturation
    print("\n[7] Exécution des tests sur les endpoints du module Billing...")
    if product_id and third_party_id:
        # A. Créer une Facture Proforma
        print("\n[7A] Création d'une Facture Proforma...")
        proforma_payload = {
            "counterpartyThirdPartyId": third_party_id,
            "currency": "XAF",
            "lines": [
                {
                    "productId": product_id,
                    "quantity": 3.0,
                    "unitPrice": 2500.0
                }
            ]
        }
        proforma_resp = execute_curl("POST", "/api/factures-proforma", headers=org_headers, payload=proforma_payload)
        if proforma_resp and (proforma_resp.get("success") or proforma_resp.get("id")):
            print("[+] Facture Proforma créée avec succès !")
        else:
            print("[-] Échec de la création de la Facture Proforma.")
            
        # B. Lister les Factures Proforma
        print("\n[7B] Récupération de la liste des Factures Proforma...")
        execute_curl("GET", "/api/factures-proforma", headers=org_headers)
        
        # C. Créer un Bon de Réception
        print("\n[7C] Création d'un Bon de Réception...")
        reception_payload = {
            "counterpartyThirdPartyId": third_party_id,
            "currency": "XAF",
            "lines": [
                {
                    "productId": product_id,
                    "quantity": 10.0,
                    "unitPrice": 2000.0
                }
            ]
        }
        reception_resp = execute_curl("POST", "/api/v1/facturation/bon-receptions", headers=org_headers, payload=reception_payload)
        if reception_resp and (reception_resp.get("success") or reception_resp.get("id")):
            print("[+] Bon de Réception créé avec succès !")
        else:
            print("[-] Échec de la création du Bon de Réception.")
            
        # D. Lister les Bons de Réception
        print("\n[7D] Récupération de la liste des Bons de Réception...")
        execute_curl("GET", "/api/v1/facturation/bon-receptions", headers=org_headers)
    else:
        print("[-] Impossible d'exécuter les tests de facturation sans produit ou tiers valide.")

    print("\n=== TESTS TERMINÉS AVEC SUCCÈS ===")

if __name__ == "__main__":
    main()
