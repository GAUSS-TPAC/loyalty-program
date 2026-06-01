package com.yowyob.loyaulty.program.api.reward.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRewardRequest(
        @NotBlank String name,
        String description,
        @NotBlank String type,
        @NotNull @Min(0) Long costPoints,
        Integer stock,
        String validFrom,
        String validUntil
) {}
