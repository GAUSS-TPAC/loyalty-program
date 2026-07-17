// Démarrage rapide — node examples/quickstart.mjs (après `npm run build`)
import { LoyaltyClient, ApiError, SignatureVerificationError } from "../dist/index.js";

// 1. Initialisation — récupérez ces clés dans Portail > Applications
const loyalty = new LoyaltyClient(
    process.env.LOYALTY_PUBLIC_KEY ?? "pk_test_xxx",
    process.env.LOYALTY_PRIVATE_KEY ?? "sk_test_xxx",
    process.env.LOYALTY_BASE_URL ?? "http://localhost:8081",
    { webhookSecret: process.env.LOYALTY_WEBHOOK_SECRET }
);

// 2. Envoyer un événement d'achat (avec clé d'idempotence)
try {
    const result = await loyalty.trackEvent("purchase.completed", "00000000-0000-0000-0000-000000000123", {
        payload: { amount: 4990 },
        idempotencyKey: "commande-2026-0042",
    });
    console.log("Événement traité :", result.eventId);
    for (const effect of result.effectsApplied) {
        console.log(` → effet : ${effect.effectType} (règle : ${effect.ruleName})`);
    }
} catch (e) {
    if (e instanceof ApiError) console.error(`Erreur API (${e.statusCode}) : ${e.message}`);
    else throw e;
}

// 3. Consulter le solde de points du membre
const points = await loyalty.getMemberPoints("00000000-0000-0000-0000-000000000123");
console.log("Points disponibles :", points.availablePoints);

// 4. Dans votre route de callback Express :
//    app.post("/loyalty/callback", express.raw({ type: "application/json" }), (req, res) => {
//      try {
//        const event = loyalty.checkCallbackIntegrity(req.headers, req.body);
//        res.status(200).send("OK");
//      } catch (e) {
//        if (e instanceof SignatureVerificationError) res.status(400).send("KO");
//        else throw e;
//      }
//    });
