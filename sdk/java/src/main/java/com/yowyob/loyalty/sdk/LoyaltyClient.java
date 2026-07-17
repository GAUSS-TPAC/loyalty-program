package com.yowyob.loyalty.sdk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Client officiel de l'API Yowyob Loyalty.
 *
 * <pre>{@code
 * LoyaltyClient loyalty = new LoyaltyClient(
 *         System.getenv("LOYALTY_PUBLIC_KEY"),    // pk_live_… (identifiant, exposable)
 *         System.getenv("LOYALTY_PRIVATE_KEY"),   // sk_live_… (SECRÈTE — jamais dans Git)
 *         "https://loyalty.yowyob.com",
 *         System.getenv("LOYALTY_WEBHOOK_SECRET") // whsec_… (pour checkCallbackIntegrity)
 * );
 * Map<String, Object> result = loyalty.trackEvent(
 *         "purchase.completed", memberId, null, Map.of("amount", 4990), "commande-42");
 * }</pre>
 */
public class LoyaltyClient {

    public static final int DEFAULT_SIGNATURE_TOLERANCE_SECONDS = 300;

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final String publicKey;
    private final String privateKey;
    private final String baseUrl;
    private final String webhookSecret;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Duration timeout;

    public LoyaltyClient(String publicKey, String privateKey, String baseUrl) {
        this(publicKey, privateKey, baseUrl, null);
    }

    public LoyaltyClient(String publicKey, String privateKey, String baseUrl, String webhookSecret) {
        this(publicKey, privateKey, baseUrl, webhookSecret, Duration.ofSeconds(10));
    }

    public LoyaltyClient(String publicKey, String privateKey, String baseUrl, String webhookSecret, Duration timeout) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.webhookSecret = webhookSecret;
        this.timeout = timeout;
        this.httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
    }

    /**
     * Envoie un événement métier (achat, trajet, inscription…) au moteur de fidélité.
     *
     * @param eventType      type d'événement, ex. "purchase.completed"
     * @param memberId       identifiant du membre dans votre système (UUID)
     * @param occurredAt     date de l'événement (null = maintenant)
     * @param payload        données métier lues par les règles (ex. Map.of("amount", 4990))
     * @param idempotencyKey le même événement renvoyé deux fois avec la même clé
     *                       n'est traité qu'une seule fois (null = pas d'idempotence)
     * @return réponse : eventId, effectsApplied, notifications, processedAt
     */
    public Map<String, Object> trackEvent(String eventType, String memberId, Instant occurredAt,
                                          Map<String, Object> payload, String idempotencyKey) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("eventType", eventType);
        body.put("memberId", memberId);
        body.put("occurredAt", (occurredAt != null ? occurredAt : Instant.now()).toString());
        body.put("payload", payload != null ? payload : Map.of());

        Map<String, String> headers = new HashMap<>();
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            headers.put("Idempotency-Key", idempotencyKey);
        }
        return request("POST", "/api/v1/apps/" + publicKey + "/events", body, headers);
    }

    /** Solde de points et palier courant d'un membre. */
    public Map<String, Object> getMemberPoints(String memberId) {
        return request("GET", "/api/v1/members/" + memberId + "/points", null, Map.of());
    }

    /** Palier de fidélité courant d'un membre. */
    public Map<String, Object> getMemberTier(String memberId) {
        return request("GET", "/api/v1/members/" + memberId + "/tier", null, Map.of());
    }

    /** Portefeuille (wallet) d'un membre : solde monétaire et politique associée. */
    public Map<String, Object> getWallet(String memberId) {
        return request("GET", "/api/v1/members/" + memberId + "/wallet", null, Map.of());
    }

    /**
     * Vérifie l'authenticité d'un callback webhook reçu de la plateforme Loyalty.
     *
     * <p>La plateforme signe chaque callback avec le SECRET WEBHOOK (whsec_…, différent de la
     * clé privée) : {@code signature = "sha256=" + hex(HMAC-SHA256(secret, timestamp + "." + corps brut))}.
     *
     * @param headers          en-têtes HTTP reçus (insensible à la casse) — doit contenir
     *                         X-Webhook-Signature et X-Webhook-Timestamp
     * @param rawBody          corps BRUT de la requête (avant tout parsing JSON)
     * @param toleranceSeconds tolérance d'horodatage anti-rejeu (300 s recommandé)
     * @return le payload décodé, une fois la signature validée
     * @throws SignatureVerificationException si la requête est forgée, altérée ou rejouée
     */
    public Map<String, Object> checkCallbackIntegrity(Map<String, String> headers, String rawBody,
                                                      int toleranceSeconds) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new LoyaltyException(
                    "webhookSecret non configuré : passez-le au constructeur de LoyaltyClient.");
        }

        Map<String, String> normalized = new HashMap<>();
        headers.forEach((name, value) -> normalized.put(name.toLowerCase(Locale.ROOT), value));
        String signature = normalized.get("x-webhook-signature");
        String timestamp = normalized.get("x-webhook-timestamp");
        if (signature == null || timestamp == null) {
            throw new SignatureVerificationException(
                    "En-têtes X-Webhook-Signature ou X-Webhook-Timestamp absents.");
        }
        long timestampValue;
        try {
            timestampValue = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new SignatureVerificationException("X-Webhook-Timestamp illisible.");
        }
        if (Math.abs(Instant.now().getEpochSecond() - timestampValue) > toleranceSeconds) {
            throw new SignatureVerificationException("Horodatage du callback hors tolérance (rejeu possible).");
        }

        String expected = "sha256=" + hmacSha256Hex(webhookSecret, timestamp + "." + rawBody);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8))) {
            throw new SignatureVerificationException("Signature du callback invalide.");
        }

        try {
            return objectMapper.readValue(rawBody, MAP_TYPE);
        } catch (IOException e) {
            throw new SignatureVerificationException("Corps du callback illisible (JSON attendu).");
        }
    }

    /** Variante avec la tolérance par défaut de 300 secondes. */
    public Map<String, Object> checkCallbackIntegrity(Map<String, String> headers, String rawBody) {
        return checkCallbackIntegrity(headers, rawBody, DEFAULT_SIGNATURE_TOLERANCE_SECONDS);
    }

    public String getPublicKey() {
        return publicKey;
    }

    private Map<String, Object> request(String method, String path, Map<String, Object> body,
                                        Map<String, String> extraHeaders) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(timeout)
                .header("Accept", "application/json")
                .header("X-Api-Key", privateKey);
        extraHeaders.forEach(builder::header);

        if (body != null) {
            String json;
            try {
                json = objectMapper.writeValueAsString(body);
            } catch (IOException e) {
                throw new LoyaltyException("Impossible de sérialiser la requête.", e);
            }
            builder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        HttpResponse<String> response;
        try {
            response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new ApiException("Erreur réseau vers l'API Loyalty : " + e.getMessage(), 0, null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Appel à l'API Loyalty interrompu.", 0, null);
        }

        Map<String, Object> decoded = decode(response.body());
        int status = response.statusCode();
        if (status == 401 || status == 403) {
            throw new AuthenticationException("Clé API invalide ou révoquée (HTTP " + status + ").", status, decoded);
        }
        if (status < 200 || status >= 300) {
            String message = null;
            if (decoded != null) {
                Object detail = decoded.getOrDefault("detail", decoded.get("title"));
                if (detail != null) message = detail.toString();
            }
            throw new ApiException(message != null ? message : "Erreur API Loyalty (HTTP " + status + ").",
                    status, decoded);
        }
        return decoded != null ? decoded : Map.of();
    }

    private Map<String, Object> decode(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return objectMapper.readValue(raw, MAP_TYPE);
        } catch (IOException e) {
            return null;
        }
    }

    private static String hmacSha256Hex(String secret, String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new LoyaltyException("HMAC-SHA256 indisponible.", e);
        }
    }
}
