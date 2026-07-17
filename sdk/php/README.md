# Yowyob Loyalty — SDK PHP

SDK officiel pour intégrer la plateforme de fidélité **Yowyob Loyalty** dans une application PHP (Laravel, Symfony, WordPress, vanilla…).

## Installation

```bash
composer require yowyob/loyalty-sdk
```

En attendant la publication sur Packagist, ajoutez le dépôt en local :

```json
{
  "repositories": [{ "type": "path", "url": "../loyalty-program/sdk/php" }],
  "require": { "yowyob/loyalty-sdk": "*" }
}
```

## Prérequis

1. Créez une **Application** dans le portail (Portail → Applications).
2. Notez la **clé publique** (`pk_live_…` / `pk_test_…`), la **clé privée** (`sk_live_…` / `sk_test_…`, affichée une seule fois) et, si vous avez fourni une URL de callback, le **secret webhook** (`whsec_…`, affiché une seule fois).
3. Gardez la clé privée et le secret webhook **hors du code source** (variables d'environnement). La clé publique, elle, peut être exposée sans risque.

## Démarrage rapide

```php
use Yowyob\Loyalty\LoyaltyClient;

$loyalty = new LoyaltyClient(
    $_ENV['LOYALTY_PUBLIC_KEY'],
    $_ENV['LOYALTY_PRIVATE_KEY'],
    'https://loyalty.yowyob.com',
    $_ENV['LOYALTY_WEBHOOK_SECRET'] ?? null
);

// Envoyer un événement métier
$result = $loyalty->trackEvent(
    'purchase.completed',
    $memberId,
    null,                        // occurredAt (défaut : maintenant)
    ['amount' => 4990],          // payload lu par vos règles de fidélité
    'commande-' . $orderId       // clé d'idempotence : rejouable sans double crédit
);

// Lire le solde / palier / wallet d'un membre
$points = $loyalty->getMemberPoints($memberId);
$tier   = $loyalty->getMemberTier($memberId);
$wallet = $loyalty->getWallet($memberId);
```

Utilisez une clé `TEST` (`sk_test_…`) pendant l'intégration, puis basculez sur la clé `LIVE` en production.

## Vérifier les callbacks (webhooks)

La plateforme notifie votre URL de callback à chaque événement de fidélité (`points.earned`, `tier.changed`…). Chaque requête est signée : **vérifiez toujours la signature avant de traiter**.

> ⚠️ La signature utilise le **secret webhook** (`whsec_…`), pas la clé privée — contrairement à d'autres plateformes de paiement.

```php
try {
    $event = $loyalty->checkCallbackIntegrity(getallheaders(), file_get_contents('php://input'));
    // $event = ['id' => ..., 'type' => 'points.earned', 'application' => 'pk_live_...', 'data' => [...]]
    http_response_code(200);
    echo 'OK';
} catch (\Yowyob\Loyalty\Exception\SignatureVerificationException $e) {
    http_response_code(400);
    echo 'KO';
}
```

Détails : en-têtes `X-Webhook-Signature` (`sha256=<hex>`), `X-Webhook-Timestamp` (epoch secondes), `X-Webhook-Id`. Signature = `HMAC-SHA256(secret, timestamp + "." + corps brut)`. Les livraisons échouées sont retentées jusqu'à 6 fois avec backoff exponentiel.

## Gestion des erreurs

| Exception | Cas |
|---|---|
| `AuthenticationException` | Clé privée invalide/révoquée (401/403) |
| `ApiException` | Toute autre erreur API (`getStatusCode()`, `getBody()`) |
| `SignatureVerificationException` | Callback forgé, altéré ou rejoué |

## API de référence

| Méthode | Endpoint appelé |
|---|---|
| `trackEvent($type, $memberId, $occurredAt, $payload, $idempotencyKey)` | `POST /api/v1/apps/{publicKey}/events` |
| `getMemberPoints($memberId)` | `GET /api/v1/members/{id}/points` |
| `getMemberTier($memberId)` | `GET /api/v1/members/{id}/tier` |
| `getWallet($memberId)` | `GET /api/v1/members/{id}/wallet` |
| `checkCallbackIntegrity($headers, $rawBody, $tolerance = 300)` | — (vérification locale) |
