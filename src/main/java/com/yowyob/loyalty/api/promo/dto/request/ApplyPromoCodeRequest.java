package com.yowyob.loyalty.api.promo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ApplyPromoCodeRequest(
        @NotBlank String code,
        @NotBlank String orderId,
        @NotNull @Positive BigDecimal orderAmount
) {}
