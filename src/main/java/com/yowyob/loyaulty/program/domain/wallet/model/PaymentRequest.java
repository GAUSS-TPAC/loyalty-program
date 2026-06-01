package com.yowyob.loyaulty.program.domain.wallet.model;

import com.yowyob.loyaulty.program.domain.wallet.model.enums.PaymentDirection;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.PaymentProvider;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.PaymentRequestStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entité représentant une requête de paiement vers un prestataire externe.
 *
 * <p>Une PaymentRequest est créée pour chaque interaction avec un provider
 * (MTN, Orange, Stripe). Elle est liée à une WalletTransaction et son état
 * évolue en fonction des callbacks/webhooks reçus.</p>
 *
 * <p>Cycle de vie attendu :</p>
 * <pre>
 * INITIATED → PROCESSING → CONFIRMED  ✓ (webhook succès)
 *                       → FAILED      ✗ (webhook échec)
 *                       → TIMEOUT     ⏱ (pas de réponse dans le délai)
 *                       → CANCELLED   ✕ (annulation explicite)
 * </pre>
 */
@Getter
@Builder(toBuilder = true)
@With
public class PaymentRequest {

    // ── Identité ─────────────────────────────────────────────────────────────

    /** Identifiant unique de la PaymentRequest. */
    private final UUID id;

    /** Identifiant du wallet concerné. */
    private final UUID walletId;

    /** Identifiant du tenant. */
    private final UUID tenantId;

    /**
     * Identifiant de la WalletTransaction associée.
     * Créée en même temps que la PaymentRequest (statut PENDING).
     */
    private final UUID walletTransactionId;

    // ── Provider & direction ──────────────────────────────────────────────────

    /** Prestataire de paiement cible (MTN, ORANGE, STRIPE…). */
    private final PaymentProvider provider;

    /** Direction du flux : INBOUND (recharge) ou OUTBOUND (retrait). */
    private final PaymentDirection direction;

    // ── Montant ───────────────────────────────────────────────────────────────

    /**
     * Montant de l'opération, toujours positif.
     */
    private final BigDecimal amount;

    /** Code devise ISO 4217 (ex. "XAF", "EUR"). */
    private final String currency;

    // ── Identifiants côté provider ────────────────────────────────────────────

    /**
     * Référence externe générée par le provider (retournée dans la réponse initiale).
     * Utilisée pour le rapprochement lors de la réception des webhooks.
     * Null tant que le provider n'a pas répondu.
     */
    private final String externalReference;

    /**
     * Numéro de téléphone Mobile Money du membre (pour MTN et Orange).
     * Null pour les paiements Stripe.
     */
    private final String mobileMoneyPhoneNumber;

    // ── État & retry ──────────────────────────────────────────────────────────

    /** État courant de la PaymentRequest. */
    private final PaymentRequestStatus status;

    /**
     * Nombre de tentatives effectuées (pour la gestion des retries en cas de timeout).
     */
    private final int retryCount;

    /**
     * Nombre maximum de tentatives autorisées avant de passer en FAILED.
     * Défini par la configuration du tenant ou du provider.
     */
    private final int maxRetries;

    // ── Horodatages ──────────────────────────────────────────────────────────

    /** Date/heure de création de la requête (UTC). */
    private final Instant createdAt;

    /** Date/heure de la prochaine tentative si la précédente a expiré. */
    private final Instant nextRetryAt;

    /** Date/heure d'expiration (timeout) si aucun webhook n'est reçu avant. */
    private final Instant expiresAt;

    /**
     * Date/heure à laquelle la réponse finale a été reçue
     * (CONFIRMED, FAILED, TIMEOUT ou CANCELLED).
     */
    private final Instant resolvedAt;

    // ── Informations de réponse ───────────────────────────────────────────────

    /**
     * Message d'erreur retourné par le provider (si status = FAILED).
     * Null sinon.
     */
    private final String providerErrorMessage;

    /**
     * Payload brut du webhook reçu du provider (stocké pour audit).
     */
    private final String webhookPayload;

    // ── Méthodes de domaine ───────────────────────────────────────────────────

    /**
     * Indique si la PaymentRequest peut encore faire l'objet d'un retry.
     *
     * @return true si retryCount < maxRetries et status est TIMEOUT.
     */
    public boolean canRetry() {
        return PaymentRequestStatus.TIMEOUT.equals(status) && retryCount < maxRetries;
    }

    /**
     * Indique si la PaymentRequest est dans un état terminal.
     *
     * @return true si status est CONFIRMED, FAILED, TIMEOUT (épuisé) ou CANCELLED.
     */
    public boolean isTerminal() {
        return switch (status) {
            case CONFIRMED, FAILED, CANCELLED -> true;
            case TIMEOUT -> retryCount >= maxRetries;
            default -> false;
        };
    }

    /**
     * Vérifie si la PaymentRequest a expiré sans réponse du provider.
     *
     * @return true si l'heure actuelle est après {@code expiresAt} et status est PROCESSING.
     */
    public boolean hasExpired() {
        return PaymentRequestStatus.PROCESSING.equals(status)
                && expiresAt != null
                && Instant.now().isAfter(expiresAt);
    }
}
