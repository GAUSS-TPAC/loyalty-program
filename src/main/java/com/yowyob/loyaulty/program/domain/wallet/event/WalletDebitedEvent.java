package com.yowyob.loyaulty.program.domain.wallet.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine émis après le débit réussi d'un wallet.
 * Topic Kafka : {@code wallet.debited}
 *
 * @param eventId        identifiant unique de l'événement.
 * @param walletId       identifiant du wallet débité.
 * @param memberId       identifiant du membre.
 * @param tenantId       identifiant du tenant.
 * @param amount         montant débité.
 * @param currency       devise (ISO 4217).
 * @param source         source du débit (ex. "PURCHASE", "WITHDRAWAL_MTN").
 * @param balanceAfter   solde disponible après débit.
 * @param transactionId  identifiant de la WalletTransaction créée.
 * @param occurredAt     horodatage UTC de l'événement.
 */
public record WalletDebitedEvent(
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
    public static WalletDebitedEvent of(
            UUID walletId, UUID memberId, UUID tenantId,
            BigDecimal amount, String currency, String source,
            BigDecimal balanceAfter, UUID transactionId) {
        return new WalletDebitedEvent(
                UUID.randomUUID(), walletId, memberId, tenantId,
                amount, currency, source, balanceAfter, transactionId, Instant.now()
        );
    }
}
