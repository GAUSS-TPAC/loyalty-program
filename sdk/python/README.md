# Yowyob Loyalty — SDK Python

SDK officiel pour intégrer la plateforme de fidélité **Yowyob Loyalty** dans une application Python (Django, Flask, FastAPI…). Python ≥ 3.9, zéro dépendance (stdlib uniquement).

## Installation

```bash
pip install yowyob-loyalty-sdk
# en local, avant publication PyPI :
pip install /chemin/vers/loyalty-program/sdk/python
```

## Prérequis

1. Créez une **Application** dans le portail (Portail → Applications).
2. Notez la **clé publique** (`pk_live_…`), la **clé privée** (`sk_live_…`, affichée une seule fois) et le **secret webhook** (`whsec_…`) si vous avez fourni une URL de callback.
3. Stockez la clé privée et le secret webhook dans des variables d'environnement, jamais dans Git.

## Démarrage rapide

```python
import os
from loyalty_sdk import LoyaltyClient

loyalty = LoyaltyClient(
    public_key=os.environ["LOYALTY_PUBLIC_KEY"],
    private_key=os.environ["LOYALTY_PRIVATE_KEY"],
    base_url="https://loyalty.yowyob.com",
    webhook_secret=os.environ.get("LOYALTY_WEBHOOK_SECRET"),
)

# Envoyer un événement métier
result = loyalty.track_event(
    "purchase.completed",
    member_id,
    payload={"amount": 4990},
    idempotency_key=f"commande-{order_id}",   # rejouable sans double crédit
)

# Lire le solde / palier / wallet d'un membre
points = loyalty.get_member_points(member_id)
tier = loyalty.get_member_tier(member_id)
wallet = loyalty.get_wallet(member_id)
```

Utilisez une clé `TEST` (`sk_test_…`) pendant l'intégration, puis basculez sur la clé `LIVE` en production.

## Vérifier les callbacks (webhooks)

> ⚠️ La signature utilise le **secret webhook** (`whsec_…`), pas la clé privée.

```python
from flask import Flask, request
from loyalty_sdk import SignatureVerificationError

app = Flask(__name__)

@app.post("/loyalty/callback")
def loyalty_callback():
    try:
        # request.get_data() = corps BRUT, indispensable pour la signature
        event = loyalty.check_callback_integrity(request.headers, request.get_data())
        # event = {"id": ..., "type": "points.earned", "application": "pk_live_…", "data": {...}}
        return "OK", 200
    except SignatureVerificationError:
        return "KO", 400
```

Détails : en-têtes `X-Webhook-Signature` (`sha256=<hex>`), `X-Webhook-Timestamp` (epoch secondes), `X-Webhook-Id`. Signature = `HMAC-SHA256(secret, timestamp + "." + corps brut)`, comparaison en temps constant (`hmac.compare_digest`), tolérance d'horodatage 300 s. Les livraisons échouées sont retentées jusqu'à 6 fois avec backoff exponentiel.

## Gestion des erreurs

| Exception | Cas |
|---|---|
| `AuthenticationError` | Clé privée invalide/révoquée (401/403) |
| `ApiError` | Toute autre erreur API (`status_code`, `body`) |
| `SignatureVerificationError` | Callback forgé, altéré ou rejoué |
