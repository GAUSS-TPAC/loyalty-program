package com.yowyob.loyaulty.program.domain.loyalty.model;

import java.math.BigDecimal;

public record BonificationReward(
        String rewardId,
        String name,
        String description,
        String type,
        BigDecimal value
) {}
