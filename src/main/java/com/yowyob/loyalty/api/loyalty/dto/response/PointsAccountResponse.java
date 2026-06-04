package com.yowyob.loyalty.api.loyalty.dto.response;

import com.yowyob.loyalty.domain.loyalty.model.points.PointsAccount;
import com.yowyob.loyalty.domain.loyalty.model.tier.MemberTier;

import java.math.BigDecimal;

public record PointsAccountResponse(
        long availablePoints,
        long lifetimeEarned,
        long lifetimeSpent,
        String tierLevel,
        BigDecimal tierMultiplier
) {
    public static PointsAccountResponse from(PointsAccount account, MemberTier tier) {
        return new PointsAccountResponse(
                account.getAvailablePoints(),
                account.getLifetimeEarned(),
                account.getLifetimeSpent(),
                tier.level().name(),
                tier.pointsMultiplier()
        );
    }
}
