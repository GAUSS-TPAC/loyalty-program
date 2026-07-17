<?php

require __DIR__ . '/../vendor/autoload.php';

use Yowyob\Loyalty\LoyaltyClient;
use Yowyob\Loyalty\Exception\ApiException;
use Yowyob\Loyalty\Exception\SignatureVerificationException;

// 1. Initialisation — récupérez ces clés dans Portail > Applications
$loyalty = new LoyaltyClient(
    getenv('LOYALTY_PUBLIC_KEY') ?: 'pk_test_xxx',
    getenv('LOYALTY_PRIVATE_KEY') ?: 'sk_test_xxx',
    getenv('LOYALTY_BASE_URL') ?: 'http://localhost:8081',
    getenv('LOYALTY_WEBHOOK_SECRET') ?: null
);

// 2. Envoyer un événement d'achat (avec clé d'idempotence)
try {
    $result = $loyalty->trackEvent(
        'purchase.completed',
        '00000000-0000-0000-0000-000000000123',
        null,
        ['amount' => 4990],
        'commande-2026-0042'
    );
    echo "Événement traité : " . $result['eventId'] . PHP_EOL;
    foreach ($result['effectsApplied'] as $effect) {
        echo " → effet : " . $effect['effectType'] . " (règle : " . $effect['ruleName'] . ")" . PHP_EOL;
    }
} catch (ApiException $e) {
    echo "Erreur API (" . $e->getStatusCode() . ") : " . $e->getMessage() . PHP_EOL;
}

// 3. Consulter le solde de points du membre
$points = $loyalty->getMemberPoints('00000000-0000-0000-0000-000000000123');
echo "Points disponibles : " . $points['availablePoints'] . PHP_EOL;

// 4. Dans votre contrôleur de callback (ex. /loyalty/callback) :
//    $payload = $loyalty->checkCallbackIntegrity(getallheaders(), file_get_contents('php://input'));
//    → lève SignatureVerificationException si la requête est forgée ; répondez alors 400.
