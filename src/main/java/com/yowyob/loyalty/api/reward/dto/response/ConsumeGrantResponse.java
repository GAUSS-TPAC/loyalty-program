package com.yowyob.loyalty.api.reward.dto.response;

import com.yowyob.loyalty.domain.reward.model.ConsumeGrantResult;

import java.math.BigDecimal;
import java.util.UUID;

public record ConsumeGrantResponse(
        UUID grantId,
        String rewardType,
        BigDecimal discountApplied,
        BigDecimal finalOrderAmount,
        boolean fullyConsumed,
        String grantStatus
) {
    public static ConsumeGrantResponse from(ConsumeGrantResult result) {
        return new ConsumeGrantResponse(
                result.updatedGrant().id(),
                result.updatedGrant().rewardType().name(),
                result.discountApplied(),
                result.finalOrderAmount(),
                result.fullyConsumed(),
                result.updatedGrant().status().name()
        );
    }
}
