package com.yowyob.loyalty.api.loyalty.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record TierPolicyRequest(
        @NotBlank String criterion,
        @NotEmpty List<TierThresholdRequest> thresholds,
        @NotBlank String maintainPeriod,
        long maintainThresholdPoints,
        int downgradeGraceDays
) {
    public record TierThresholdRequest(
            @NotBlank String level,
            @NotNull long threshold,
            @NotNull BigDecimal multiplier
    ) {}
}
