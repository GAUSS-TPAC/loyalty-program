package com.yowyob.loyaulty.program.api.wallet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DebitRequest(
        @NotNull(message = "amount is required")
        @Positive(message = "amount must be positive")
        BigDecimal amount,

        @NotBlank(message = "description is required")
        String description
) {}
