package com.yowyob.loyalty.domain.loyalty.model.tier;

import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TierPolicy(
        TenantId tenantId,
        String criterion,
        List<TierThreshold> thresholds,
        String maintainPeriod,
        long maintainThresholdPoints,
        int downgradeGraceDays
) {
    public record TierThreshold(
            TierLevel level,
            long threshold,
            BigDecimal multiplier
    ) {
    }

    public TierLevel calculateTier(long criterionValue) {
        if (thresholds == null || thresholds.isEmpty()) {
            return TierLevel.BRONZE;
        }
        
        // Ensure the thresholds are evaluated from highest to lowest
        return thresholds.stream()
                .sorted((t1, t2) -> Long.compare(t2.threshold(), t1.threshold()))
                .filter(t -> criterionValue >= t.threshold())
                .map(TierThreshold::level)
                .findFirst()
                .orElse(TierLevel.BRONZE);
    }

    public static TierPolicy defaults(TenantId tenantId) {
        return new TierPolicy(
                tenantId,
                "LIFETIME_POINTS",
                List.of(
                        new TierThreshold(TierLevel.PLATINUM, 20000, new BigDecimal("2.00")),
                        new TierThreshold(TierLevel.GOLD, 5000, new BigDecimal("1.50")),
                        new TierThreshold(TierLevel.SILVER, 1000, new BigDecimal("1.25")),
                        new TierThreshold(TierLevel.BRONZE, 0, new BigDecimal("1.00"))
                ),
                "QUARTERLY",
                0,
                30
        );
    }
}
