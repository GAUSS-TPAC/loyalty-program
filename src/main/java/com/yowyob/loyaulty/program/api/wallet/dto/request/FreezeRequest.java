package com.yowyob.loyaulty.program.api.wallet.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FreezeRequest(
        @NotBlank(message = "reason is required")
        String reason
) {}
