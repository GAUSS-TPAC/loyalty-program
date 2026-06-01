package com.yowyob.loyaulty.program.api.loyalty.dto.response;

public record PointsResultResponse(
        String transactionId,
        Integer pointsEarned,
        Integer totalPoints,
        boolean rewardTriggered,
        BonificationRewardResponse reward
) {}
