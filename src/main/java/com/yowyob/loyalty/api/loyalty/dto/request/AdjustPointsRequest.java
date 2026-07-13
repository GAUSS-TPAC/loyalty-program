package com.yowyob.loyalty.api.loyalty.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record AdjustPointsRequest(
        @Positive long amount,
        boolean debit,
        @NotBlank String reason
) {}
