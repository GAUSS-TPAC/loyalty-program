package com.yowyob.loyalty.domain.reward.model;

public record RedemptionResult(
        RewardGrant grant,
        long pointsSpent,
        long remainingPointsBalance
) {}
