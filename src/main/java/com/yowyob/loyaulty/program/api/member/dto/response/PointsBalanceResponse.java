package com.yowyob.loyaulty.program.api.member.dto.response;

import com.yowyob.loyaulty.program.domain.member.model.PointsAccount;

public record PointsBalanceResponse(
        String memberId,
        long availablePoints,
        long lifetimeEarned,
        long lifetimeSpent
) {
    public static PointsBalanceResponse from(PointsAccount a) {
        return new PointsBalanceResponse(
                a.getMemberId(),
                a.getAvailablePoints(),
                a.getLifetimeEarned(),
                a.getLifetimeSpent()
        );
    }
}
