package com.yowyob.loyalty.api.reward.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record CreateRewardRequest(
        @NotBlank String name,
        String description,
        @NotNull String type,
        @NotNull BigDecimal numericValue,
        @NotBlank String valueUnit,
        int maxApplicationCount,
        @Min(0) long costInPoints,
        Integer stockTotal,
        Instant validFrom,
        Instant validUntil,
        int grantExpiryDays,
        String imageUrl,
        Map<String, Object> metadata
) {}
