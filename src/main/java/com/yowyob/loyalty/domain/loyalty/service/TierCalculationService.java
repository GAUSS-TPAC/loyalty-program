package com.yowyob.loyalty.domain.loyalty.service;

import com.yowyob.loyalty.domain.loyalty.model.points.PointsAccount;
import com.yowyob.loyalty.domain.loyalty.model.tier.MemberTier;
import com.yowyob.loyalty.domain.loyalty.model.tier.TierLevel;
import com.yowyob.loyalty.domain.loyalty.model.tier.TierPolicy;

import java.util.Optional;

public class TierCalculationService {

    public Optional<TierLevel> evaluateNewTier(PointsAccount account, MemberTier currentTier, TierPolicy policy) {
        if (account == null || policy == null) {
            return Optional.empty();
        }

        long criterionValue = 0;
        if ("LIFETIME_POINTS".equals(policy.criterion())) {
            criterionValue = account.getLifetimeEarned();
        } else if ("CURRENT_POINTS".equals(policy.criterion())) {
            criterionValue = account.getAvailablePoints();
        }
        // Additional criteria like TIER_POINTS could be supported here based on counters

        TierLevel calculatedTier = policy.calculateTier(criterionValue);

        TierLevel currentLevel = currentTier != null ? currentTier.level() : TierLevel.BRONZE;

        if (calculatedTier != currentLevel) {
            return Optional.of(calculatedTier);
        }

        return Optional.empty();
    }
    
    public java.math.BigDecimal getMultiplierForTier(TierLevel level, TierPolicy policy) {
        if (policy == null || policy.thresholds() == null) {
            return java.math.BigDecimal.ONE;
        }
        
        return policy.thresholds().stream()
                .filter(t -> t.level() == level)
                .map(TierPolicy.TierThreshold::multiplier)
                .findFirst()
                .orElse(java.math.BigDecimal.ONE);
    }
}
