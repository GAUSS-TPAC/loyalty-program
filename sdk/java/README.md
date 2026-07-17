# Yowyob Loyalty — SDK Java

SDK officiel pour intégrer la plateforme de fidélité **Yowyob Loyalty** dans une application Java (Spring Boot, Jakarta EE, desktop…). Java ≥ 17, seule dépendance : `jackson-databind`.

## Installation

```bash
# depuis ce dossier
mvn install
```

Puis dans votre projet :

```xml
<dependency>
    <groupId>com.yowyob</groupId>
    <artifactId>loyalty-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Prérequis

1. Créez une **Application** dans le portail (Portail → Applications).
2. Notez la **clé publique** (`pk_live_…`), la **clé privée** (`sk_live_…`, affichée une seule fois) et le **secret webhook** (`whsec_…`) si vous avez fourni une URL de callback.
3. Stockez la clé privée et le secret webhook dans des variables d'environnement, jamais dans Git.

## Démarrage rapide

```java
LoyaltyClient loyalty = new LoyaltyClient(
        System.getenv("LOYALTY_PUBLIC_KEY"),
        System.getenv("LOYALTY_PRIVATE_KEY"),
        "https://loyalty.yowyob.com",
        System.getenv("LOYALTY_WEBHOOK_SECRET"));

// Envoyer un événement métier
Map<String, Object> result = loyalty.trackEvent(
        "purchase.completed", memberId, null,
        Map.of("amount", 4990),
        "commande-" + orderId);   // clé d'idempotence : rejouable sans double crédit

// Lire le solde / palier / wallet d'un membre
Map<String, Object> points = loyalty.getMemberPoints(memberId);
Map<String, Object> tier   = loyalty.getMemberTier(memberId);
Map<String, Object> wallet = loyalty.getWallet(memberId);
```

Utilisez une clé `TEST` (`sk_test_…`) pendant l'intégration, puis basculez sur la clé `LIVE` en production.

## Vérifier les callbacks (webhooks)

> ⚠️ La signature utilise le **secret webhook** (`whsec_…`), pas la clé privée.

```java
@PostMapping("/loyalty/callback")
public ResponseEntity<String> callback(@RequestHeader Map<String, String> headers,
                                       @RequestBody String rawBody) {
    try {
        Map<String, Object> event = loyalty.checkCallbackIntegrity(headers, rawBody);
        // event = { id, type: "points.earned", application: "pk_live_…", data: {…} }
        return ResponseEntity.ok("OK");
    } catch (SignatureVerificationException e) {
        return ResponseEntity.badRequest().body("KO");
    }
}
```

Détails : en-têtes `X-Webhook-Signature` (`sha256=<hex>`), `X-Webhook-Timestamp` (epoch secondes), `X-Webhook-Id`. Signature = `HMAC-SHA256(secret, timestamp + "." + corps brut)`, comparaison en temps constant (`MessageDigest.isEqual`), tolérance d'horodatage 300 s. Les livraisons échouées sont retentées jusqu'à 6 fois avec backoff exponentiel.

## Gestion des erreurs

| Exception | Cas |
|---|---|
| `AuthenticationException` | Clé privée invalide/révoquée (401/403) |
| `ApiException` | Toute autre erreur API (`getStatusCode()`, `getBody()`) |
| `SignatureVerificationException` | Callback forgé, altéré ou rejoué |
