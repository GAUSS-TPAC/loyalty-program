package com.yowyob.loyaulty.program.domain.wallet.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine émis après le crédit réussi d'un wallet.
 * Topic Kafka : {@code wallet.credited}
 *
 * @param eventId        identifiant unique de l'événement.
 * @param walletId       identifiant du wallet crédité.
 * @param memberId       identifiant du membre propriétaire.
 * @param tenantId       identifiant du tenant.
 * @param amount         montant crédité.
 * @param currency       devise (ISO 4217).
 * @param source         source du crédit (ex. "TOPUP_MTN", "LOYALTY_REWARD").
 * @param balanceAfter   solde disponible après crédit.
 * @param transactionId  identifiant de la WalletTransaction créée.
 * @param occurredAt     horodatage UTC de l'événement.
 */
public record WalletCreditedEvent(
        UUID eventId,
        UUID walletId,
        UUID memberId,
        UUID tenantId,
        BigDecimal amount,
        String currency,
        String source,
        BigDecimal balanceAfter,
        UUID transactionId,
        Instant occurredAt
) {
    public static WalletCreditedEvent of(
            UUID walletId, UUID memberId, UUID tenantId,
            BigDecimal amount, String currency, String source,
            BigDecimal balanceAfter, UUID transactionId) {
        return new WalletCreditedEvent(
                UUID.randomUUID(), walletId, memberId, tenantId,
                amount, currency, source, balanceAfter, transactionId, Instant.now()
        );
    }
}
