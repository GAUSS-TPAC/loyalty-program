# Yowyob Loyalty — SDK JavaScript / TypeScript

SDK officiel pour intégrer la plateforme de fidélité **Yowyob Loyalty** dans une application Node.js (Express, Next.js, NestJS…). Node ≥ 18, zéro dépendance d'exécution, types TypeScript inclus.

> ⚠️ Ce SDK utilise votre **clé privée** : il s'exécute **côté serveur uniquement**, jamais dans le navigateur.

## Installation

```bash
npm install @yowyob/loyalty-sdk
# en local, avant publication npm :
npm install /chemin/vers/loyalty-program/sdk/javascript
```

## Prérequis

1. Créez une **Application** dans le portail (Portail → Applications).
2. Notez la **clé publique** (`pk_live_…`), la **clé privée** (`sk_live_…`, affichée une seule fois) et le **secret webhook** (`whsec_…`) si vous avez fourni une URL de callback.
3. Stockez la clé privée et le secret webhook dans des variables d'environnement, jamais dans Git.

## Démarrage rapide

```ts
import { LoyaltyClient } from "@yowyob/loyalty-sdk";

const loyalty = new LoyaltyClient(
  process.env.LOYALTY_PUBLIC_KEY!,
  process.env.LOYALTY_PRIVATE_KEY!,
  "https://loyalty.yowyob.com",
  { webhookSecret: process.env.LOYALTY_WEBHOOK_SECRET }
);

// Envoyer un événement métier
const result = await loyalty.trackEvent("purchase.completed", memberId, {
  payload: { amount: 4990 },
  idempotencyKey: `commande-${orderId}`,   // rejouable sans double crédit
});

// Lire le solde / palier / wallet d'un membre
const points = await loyalty.getMemberPoints(memberId);
const tier   = await loyalty.getMemberTier(memberId);
const wallet = await loyalty.getWallet(memberId);
```

Utilisez une clé `TEST` (`sk_test_…`) pendant l'intégration, puis basculez sur la clé `LIVE` en production.

## Vérifier les callbacks (webhooks)

> ⚠️ La signature utilise le **secret webhook** (`whsec_…`), pas la clé privée.

```ts
import express from "express";
import { SignatureVerificationError } from "@yowyob/loyalty-sdk";

const app = express();

// IMPORTANT : corps BRUT sur la route de callback (pas express.json())
app.post("/loyalty/callback", express.raw({ type: "application/json" }), (req, res) => {
  try {
    const event = loyalty.checkCallbackIntegrity(req.headers, req.body);
    // event = { id, type: "points.earned", application: "pk_live_…", data: {…} }
    res.status(200).send("OK");
  } catch (e) {
    if (e instanceof SignatureVerificationError) return res.status(400).send("KO");
    throw e;
  }
});
```

Détails : en-têtes `X-Webhook-Signature` (`sha256=<hex>`), `X-Webhook-Timestamp` (epoch secondes), `X-Webhook-Id`. Signature = `HMAC-SHA256(secret, timestamp + "." + corps brut)`, comparaison en temps constant, tolérance d'horodatage 300 s. Les livraisons échouées sont retentées jusqu'à 6 fois avec backoff exponentiel.

## Gestion des erreurs

| Classe | Cas |
|---|---|
| `AuthenticationError` | Clé privée invalide/révoquée (401/403) |
| `ApiError` | Toute autre erreur API (`statusCode`, `body`) |
| `SignatureVerificationError` | Callback forgé, altéré ou rejoué |

## Build

```bash
npm install
npm run build      # compile vers dist/
```
