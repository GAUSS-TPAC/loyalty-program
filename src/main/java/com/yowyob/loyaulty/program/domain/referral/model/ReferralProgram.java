package com.yowyob.loyaulty.program.domain.referral.model;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Configuration du programme de parrainage d'un tenant.
 * Un seul programme actif par tenant à la fois.
 */
public record ReferralProgram(
        UUID id,
        TenantId tenantId,
        boolean active,

        // Récompense pour le parrain (celui qui recrute)
        String referrerRewardType,   // CREDIT_POINTS | CREDIT_WALLET
        BigDecimal referrerRewardValue,

        // Récompense pour le filleul (celui qui est recruté)
        String refereeRewardType,
        BigDecimal refereeRewardValue,

        // Condition de conversion : type d'event + montant minimum
        String conversionEventType,
        BigDecimal minConversionAmount,

        // Délai max (en jours) entre inscription du filleul et conversion
        int conversionDeadlineDays
) {
    public static ReferralProgram createDefault(TenantId tenantId) {
        return new ReferralProgram(
                UUID.randomUUID(), tenantId, true,
                "CREDIT_POINTS", BigDecimal.valueOf(500),
                "CREDIT_POINTS", BigDecimal.valueOf(200),
                "purchase.completed", BigDecimal.valueOf(0),
                30
        );
    }
}
