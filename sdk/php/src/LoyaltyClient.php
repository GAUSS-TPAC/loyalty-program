<?php

namespace Yowyob\Loyalty;

use Yowyob\Loyalty\Exception\ApiException;
use Yowyob\Loyalty\Exception\AuthenticationException;
use Yowyob\Loyalty\Exception\LoyaltyException;
use Yowyob\Loyalty\Exception\SignatureVerificationException;

/**
 * Client officiel de l'API Yowyob Loyalty.
 *
 * <code>
 * $loyalty = new LoyaltyClient(
 *     'pk_live_...',          // clé publique de votre application (identifiant, exposable)
 *     'sk_live_...',          // clé privée (SECRÈTE — jamais côté navigateur, jamais dans Git)
 *     'https://loyalty.yowyob.com',
 *     'whsec_...'             // secret webhook (optionnel, requis pour checkCallbackIntegrity)
 * );
 * $result = $loyalty->trackEvent('purchase.completed', $memberId, null, ['amount' => 4990]);
 * </code>
 */
class LoyaltyClient
{
    public const DEFAULT_TIMEOUT = 10;
    public const DEFAULT_SIGNATURE_TOLERANCE = 300; // secondes

    private string $publicKey;
    private string $privateKey;
    private string $baseUrl;
    private ?string $webhookSecret;
    private int $timeout;

    public function __construct(
        string $publicKey,
        string $privateKey,
        string $baseUrl = 'https://loyalty.yowyob.com',
        ?string $webhookSecret = null,
        int $timeout = self::DEFAULT_TIMEOUT
    ) {
        $this->publicKey = $publicKey;
        $this->privateKey = $privateKey;
        $this->baseUrl = rtrim($baseUrl, '/');
        $this->webhookSecret = $webhookSecret;
        $this->timeout = $timeout;
    }

    /**
     * Envoie un événement métier (achat, trajet, inscription…) au moteur de fidélité.
     *
     * @param string      $eventType      Type d'événement, ex. "purchase.completed"
     * @param string      $memberId       Identifiant du membre dans votre système (UUID)
     * @param string|null $occurredAt     Date ISO-8601 (défaut : maintenant, UTC)
     * @param array       $payload        Données métier utilisées par les règles (ex. ["amount" => 4990])
     * @param string|null $idempotencyKey Clé d'idempotence : le même événement renvoyé deux fois
     *                                    avec la même clé n'est traité qu'une seule fois
     * @return array Réponse : eventId, effectsApplied[], notifications[], processedAt
     * @throws ApiException|AuthenticationException
     */
    public function trackEvent(
        string $eventType,
        string $memberId,
        ?string $occurredAt = null,
        array $payload = [],
        ?string $idempotencyKey = null
    ): array {
        $body = [
            'eventType' => $eventType,
            'memberId' => $memberId,
            'occurredAt' => $occurredAt ?? gmdate('Y-m-d\TH:i:s\Z'),
            'payload' => empty($payload) ? new \stdClass() : $payload,
        ];
        $headers = [];
        if ($idempotencyKey !== null) {
            $headers['Idempotency-Key'] = $idempotencyKey;
        }
        return $this->request('POST', '/api/v1/apps/' . $this->publicKey . '/events', $body, $headers);
    }

    /**
     * Solde de points et palier courant d'un membre.
     */
    public function getMemberPoints(string $memberId): array
    {
        return $this->request('GET', '/api/v1/members/' . $memberId . '/points');
    }

    /**
     * Palier de fidélité courant d'un membre.
     */
    public function getMemberTier(string $memberId): array
    {
        return $this->request('GET', '/api/v1/members/' . $memberId . '/tier');
    }

    /**
     * Portefeuille (wallet) d'un membre : solde monétaire et politique associée.
     */
    public function getWallet(string $memberId): array
    {
        return $this->request('GET', '/api/v1/members/' . $memberId . '/wallet');
    }

    /**
     * Vérifie l'authenticité d'un callback webhook reçu de la plateforme Loyalty.
     *
     * La plateforme signe chaque callback avec le SECRET WEBHOOK (whsec_..., différent
     * de la clé privée) : signature = "sha256=" + hex(HMAC-SHA256(secret, timestamp + "." + corps brut)).
     *
     * @param array  $headers          En-têtes HTTP reçus (insensible à la casse) —
     *                                 doit contenir X-Webhook-Signature et X-Webhook-Timestamp
     * @param string $rawBody          Corps BRUT de la requête (avant tout json_decode)
     * @param int    $toleranceSeconds Tolérance d'horodatage anti-rejeu (défaut 300 s)
     * @return array Le payload décodé, une fois la signature validée
     * @throws SignatureVerificationException|LoyaltyException
     */
    public function checkCallbackIntegrity(array $headers, string $rawBody, int $toleranceSeconds = self::DEFAULT_SIGNATURE_TOLERANCE): array
    {
        if ($this->webhookSecret === null) {
            throw new LoyaltyException('webhookSecret non configuré : passez-le au constructeur de LoyaltyClient.');
        }

        $normalized = array_change_key_case($headers, CASE_LOWER);
        $signature = $normalized['x-webhook-signature'] ?? null;
        $timestamp = $normalized['x-webhook-timestamp'] ?? null;
        if (is_array($signature)) $signature = $signature[0];
        if (is_array($timestamp)) $timestamp = $timestamp[0];

        if ($signature === null || $timestamp === null) {
            throw new SignatureVerificationException('En-têtes X-Webhook-Signature ou X-Webhook-Timestamp absents.');
        }
        if (abs(time() - (int) $timestamp) > $toleranceSeconds) {
            throw new SignatureVerificationException('Horodatage du callback hors tolérance (rejeu possible).');
        }

        $expected = 'sha256=' . hash_hmac('sha256', $timestamp . '.' . $rawBody, $this->webhookSecret);
        if (!hash_equals($expected, $signature)) {
            throw new SignatureVerificationException('Signature du callback invalide.');
        }

        $decoded = json_decode($rawBody, true);
        if (!is_array($decoded)) {
            throw new SignatureVerificationException('Corps du callback illisible (JSON attendu).');
        }
        return $decoded;
    }

    public function getPublicKey(): string
    {
        return $this->publicKey;
    }

    /**
     * @throws ApiException|AuthenticationException
     */
    private function request(string $method, string $path, ?array $body = null, array $extraHeaders = []): array
    {
        $ch = curl_init($this->baseUrl . $path);

        $headers = [
            'Accept: application/json',
            'X-Api-Key: ' . $this->privateKey,
        ];
        foreach ($extraHeaders as $name => $value) {
            $headers[] = $name . ': ' . $value;
        }

        curl_setopt_array($ch, [
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_CUSTOMREQUEST => $method,
            CURLOPT_TIMEOUT => $this->timeout,
        ]);
        if ($body !== null) {
            $headers[] = 'Content-Type: application/json';
            curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($body));
        }
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);

        $response = curl_exec($ch);
        if ($response === false) {
            $error = curl_error($ch);
            curl_close($ch);
            throw new ApiException('Erreur réseau vers l\'API Loyalty : ' . $error, 0);
        }
        $statusCode = (int) curl_getinfo($ch, CURLINFO_RESPONSE_CODE);
        curl_close($ch);

        $decoded = json_decode($response, true);
        $decoded = is_array($decoded) ? $decoded : null;

        if ($statusCode === 401 || $statusCode === 403) {
            throw new AuthenticationException('Clé API invalide ou révoquée (HTTP ' . $statusCode . ').', $statusCode, $decoded);
        }
        if ($statusCode < 200 || $statusCode >= 300) {
            $message = $decoded['detail'] ?? $decoded['title'] ?? ('Erreur API Loyalty (HTTP ' . $statusCode . ').');
            throw new ApiException($message, $statusCode, $decoded);
        }
        return $decoded ?? [];
    }
}
