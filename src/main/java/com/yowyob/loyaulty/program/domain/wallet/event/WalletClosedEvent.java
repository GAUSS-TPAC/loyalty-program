package com.yowyob.loyaulty.program.domain.wallet.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine émis après la clôture définitive d'un wallet.
 * Topic Kafka : {@code wallet.closed}
 *
 * @param eventId    identifiant unique de l'événement.
 * @param walletId   identifiant du wallet clôturé.
 * @param memberId   identifiant du membre.
 * @param tenantId   identifiant du tenant.
 * @param adminId    identifiant de l'administrateur ayant initié la clôture.
 * @param reason     motif de la clôture.
 * @param occurredAt horodatage UTC de l'événement.
 */
public record WalletClosedEvent(
        UUID eventId,
        UUID walletId,
        UUID memberId,
        UUID tenantId,
        String adminId,
        String reason,
        Instant occurredAt
) {
    public static WalletClosedEvent of(
            UUID walletId, UUID memberId, UUID tenantId,
            String adminId, String reason) {
        return new WalletClosedEvent(
                UUID.randomUUID(), walletId, memberId, tenantId,
                adminId, reason, Instant.now()
        );
    }
}
