package com.yowyob.loyaulty.program.domain.wallet.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine émis quand une fraude est suspectée sur un wallet.
 * Topic Kafka : {@code wallet.fraud_suspected}
 *
 * <p>Déclenche :</p>
 * <ul>
 *   <li>Une notification immédiate à l'équipe admin du tenant.</li>
 *   <li>Un gel automatique du wallet (si configuré dans la WalletPolicy).</li>
 *   <li>Une analyse approfondie par le service IA de détection de fraude.</li>
 * </ul>
 *
 * @param eventId       identifiant unique de l'événement.
 * @param walletId      identifiant du wallet suspect.
 * @param memberId      identifiant du membre.
 * @param tenantId      identifiant du tenant.
 * @param fraudType     type de fraude détectée : "RAPID_DEBITS", "UNUSUAL_AMOUNT",
 *                      "DEPOSIT_WITHDRAWAL_PATTERN", "MULTI_ACCOUNT"…
 * @param description   description détaillée de l'anomalie détectée.
 * @param riskScore     score de risque calculé (0.0 = aucun risque, 1.0 = fraude certaine).
 * @param relatedAmount montant de la transaction ayant déclenché la détection.
 * @param autoFrozen    {@code true} si le wallet a été gelé automatiquement.
 * @param occurredAt    horodatage UTC de la détection.
 */
public record WalletFraudSuspectedEvent(
        UUID eventId,
        UUID walletId,
        UUID memberId,
        UUID tenantId,
        String fraudType,
        String description,
        double riskScore,
        BigDecimal relatedAmount,
        boolean autoFrozen,
        Instant occurredAt
) {
    public static WalletFraudSuspectedEvent of(
            UUID walletId, UUID memberId, UUID tenantId,
            String fraudType, String description,
            double riskScore, BigDecimal relatedAmount, boolean autoFrozen) {
        return new WalletFraudSuspectedEvent(
                UUID.randomUUID(), walletId, memberId, tenantId,
                fraudType, description, riskScore, relatedAmount, autoFrozen, Instant.now()
        );
    }
}
