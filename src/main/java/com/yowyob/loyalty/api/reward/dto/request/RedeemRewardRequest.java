package com.yowyob.loyalty.api.reward.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RedeemRewardRequest(
        @NotNull UUID rewardId
) {}
