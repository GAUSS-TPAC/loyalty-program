package com.yowyob.loyaulty.program.api.loyalty.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BonificationTransactionRequest(
        @NotNull(message = "amount is required")
        @Positive(message = "amount must be positive")
        BigDecimal amount,

        @NotBlank(message = "description is required")
        @Size(max = 255, message = "description must not exceed 255 characters")
        String description
) {}
