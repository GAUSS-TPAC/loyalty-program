package com.yowyob.loyaulty.program.domain.wallet.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine émis après le dégel d'un wallet.
 * Topic Kafka : {@code wallet.unfrozen}
 *
 * @param eventId    identifiant unique de l'événement.
 * @param walletId   identifiant du wallet dégelé.
 * @param memberId   identifiant du membre.
 * @param tenantId   identifiant du tenant.
 * @param adminId    identifiant de l'administrateur ayant réalisé le dégel.
 * @param reason     motif du dégel.
 * @param occurredAt horodatage UTC de l'événement.
 */
public record WalletUnfrozenEvent(
        UUID eventId,
        UUID walletId,
        UUID memberId,
        UUID tenantId,
        String adminId,
        String reason,
        Instant occurredAt
) {
    public static WalletUnfrozenEvent of(
            UUID walletId, UUID memberId, UUID tenantId,
            String adminId, String reason) {
        return new WalletUnfrozenEvent(
                UUID.randomUUID(), walletId, memberId, tenantId,
                adminId, reason, Instant.now()
        );
    }
}
