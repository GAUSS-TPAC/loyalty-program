package com.yowyob.loyaulty.program.domain.wallet.event;

import com.yowyob.loyaulty.program.domain.wallet.model.enums.PaymentDirection;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.PaymentProvider;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine émis quand un provider confirme un paiement via webhook.
 * Topic Kafka : {@code payment.confirmed}
 *
 * <p>Consommé par le handler qui finalise la WalletTransaction liée
 * (PENDING → COMPLETED) et ajuste le solde du wallet.</p>
 *
 * @param eventId           identifiant unique de l'événement.
 * @param paymentRequestId  identifiant de la PaymentRequest confirmée.
 * @param walletId          identifiant du wallet impacté.
 * @param tenantId          identifiant du tenant.
 * @param provider          provider ayant confirmé le paiement.
 * @param direction         direction du flux (INBOUND = recharge, OUTBOUND = retrait).
 * @param externalReference référence externe du provider.
 * @param amount            montant confirmé.
 * @param currency          devise (ISO 4217).
 * @param occurredAt        horodatage UTC de la confirmation.
 */
public record PaymentConfirmedEvent(
        UUID eventId,
        UUID paymentRequestId,
        UUID walletId,
        UUID tenantId,
        PaymentProvider provider,
        PaymentDirection direction,
        String externalReference,
        BigDecimal amount,
        String currency,
        Instant occurredAt
) {
    public static PaymentConfirmedEvent of(
            UUID paymentRequestId, UUID walletId, UUID tenantId,
            PaymentProvider provider, PaymentDirection direction,
            String externalReference, BigDecimal amount, String currency) {
        return new PaymentConfirmedEvent(
                UUID.randomUUID(), paymentRequestId, walletId, tenantId,
                provider, direction, externalReference, amount, currency, Instant.now()
        );
    }
}
