package com.yowyob.loyalty.api.reward.dto.response;

import com.yowyob.loyalty.domain.reward.model.RedemptionResult;

public record RedemptionResponse(
        RewardGrantResponse grant,
        long pointsSpent,
        long remainingPointsBalance
) {
    public static RedemptionResponse from(RedemptionResult result) {
        return new RedemptionResponse(
                RewardGrantResponse.from(result.grant()),
                result.pointsSpent(),
                result.remainingPointsBalance()
        );
    }
}
