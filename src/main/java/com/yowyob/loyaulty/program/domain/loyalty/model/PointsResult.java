package com.yowyob.loyaulty.program.domain.loyalty.model;

public record PointsResult(
        String transactionId,
        Integer pointsEarned,
        Integer totalPoints,
        boolean rewardTriggered,
        BonificationReward triggeredReward
) {
    public boolean hasReward() {
        return rewardTriggered && triggeredReward != null;
    }

    public static PointsResult degraded() {
        return new PointsResult(null, 0, 0, false, null);
    }
}
