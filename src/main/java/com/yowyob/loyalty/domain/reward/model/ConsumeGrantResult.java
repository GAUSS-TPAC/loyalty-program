package com.yowyob.loyalty.domain.reward.model;

import java.math.BigDecimal;

public record ConsumeGrantResult(
        RewardGrant updatedGrant,
        BigDecimal discountApplied,
        BigDecimal finalOrderAmount,
        boolean fullyConsumed
) {}
