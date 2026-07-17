"""Démarrage rapide — python examples/quickstart.py"""

import os

from loyalty_sdk import ApiError, LoyaltyClient

# 1. Initialisation — récupérez ces clés dans Portail > Applications
loyalty = LoyaltyClient(
    public_key=os.environ.get("LOYALTY_PUBLIC_KEY", "pk_test_xxx"),
    private_key=os.environ.get("LOYALTY_PRIVATE_KEY", "sk_test_xxx"),
    base_url=os.environ.get("LOYALTY_BASE_URL", "http://localhost:8081"),
    webhook_secret=os.environ.get("LOYALTY_WEBHOOK_SECRET"),
)

MEMBER_ID = "00000000-0000-0000-0000-000000000123"

# 2. Envoyer un événement d'achat (avec clé d'idempotence)
try:
    result = loyalty.track_event(
        "purchase.completed",
        MEMBER_ID,
        payload={"amount": 4990},
        idempotency_key="commande-2026-0042",
    )
    print("Événement traité :", result["eventId"])
    for effect in result["effectsApplied"]:
        print(f" → effet : {effect['effectType']} (règle : {effect['ruleName']})")
except ApiError as error:
    print(f"Erreur API ({error.status_code}) : {error}")

# 3. Consulter le solde de points du membre
points = loyalty.get_member_points(MEMBER_ID)
print("Points disponibles :", points.get("availablePoints"))

# 4. Dans votre vue de callback (ex. Flask) :
#    @app.post("/loyalty/callback")
#    def loyalty_callback():
#        try:
#            event = loyalty.check_callback_integrity(request.headers, request.get_data())
#            return "OK", 200
#        except SignatureVerificationError:
#            return "KO", 400
