package com.yowyob.loyaulty.program.domain.wallet.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine émis après le gel d'un wallet.
 * Topic Kafka : {@code wallet.frozen}
 *
 * @param eventId    identifiant unique de l'événement.
 * @param walletId   identifiant du wallet gelé.
 * @param memberId   identifiant du membre.
 * @param tenantId   identifiant du tenant.
 * @param actorId    identifiant de l'acteur (admin ou "SYSTEM").
 * @param actorType  type d'acteur : "ADMIN", "SYSTEM" ou "AI_SERVICE".
 * @param reason     motif du gel.
 * @param occurredAt horodatage UTC de l'événement.
 */
public record WalletFrozenEvent(
        UUID eventId,
        UUID walletId,
        UUID memberId,
        UUID tenantId,
        String actorId,
        String actorType,
        String reason,
        Instant occurredAt
) {
    public static WalletFrozenEvent of(
            UUID walletId, UUID memberId, UUID tenantId,
            String actorId, String actorType, String reason) {
        return new WalletFrozenEvent(
                UUID.randomUUID(), walletId, memberId, tenantId,
                actorId, actorType, reason, Instant.now()
        );
    }
}
