package com.yowyob.loyalty.api.reward.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record ConsumeGrantRequestDto(
        @NotNull UUID grantId,
        @NotBlank String orderReference,
        @NotNull @Positive BigDecimal orderAmount,
        @NotNull UUID memberId
) {}
