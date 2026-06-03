package com.yowyob.loyalty.api.wallet.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record TopUpRequest(
    @NotNull @Positive BigDecimal amount, 
    @NotBlank String provider
) {}
