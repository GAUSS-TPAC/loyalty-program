package com.yowyob.loyaulty.program.api.member.dto.response;

import com.yowyob.loyaulty.program.domain.member.model.MemberTier;

public record TierResponse(
        String level,
        long lifetimePoints,
        long pointsToNextTier,
        double multiplier
) {
    public static TierResponse from(MemberTier t) {
        return new TierResponse(
                t.getLevel().name(),
                t.getLifetimePoints(),
                t.pointsToNextTier(),
                t.getMultiplier()
        );
    }
}
