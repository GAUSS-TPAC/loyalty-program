package com.yowyob.loyalty.api.loyalty.dto.response;

import com.yowyob.loyalty.domain.loyalty.model.tier.MemberTier;

import java.math.BigDecimal;
import java.time.Instant;

public record MemberTierResponse(
        String tierLevel,
        BigDecimal multiplier,
        Instant reachedAt,
        Instant validUntil
) {
    public static MemberTierResponse from(MemberTier tier) {
        return new MemberTierResponse(
                tier.level().name(),
                tier.pointsMultiplier(),
                tier.reachedAt(),
                tier.validUntil()
        );
    }
}
