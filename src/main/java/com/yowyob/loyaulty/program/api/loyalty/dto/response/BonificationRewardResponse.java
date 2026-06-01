package com.yowyob.loyaulty.program.api.loyalty.dto.response;

import java.math.BigDecimal;

public record BonificationRewardResponse(
        String rewardId,
        String name,
        String description,
        String type,
        BigDecimal value
) {}
