package com.yowyob.loyalty.api.wallet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreditRequest(
        @NotNull @Positive BigDecimal amount,
        @NotBlank String source,
        String referenceId,
        String idempotencyKey
) {}
