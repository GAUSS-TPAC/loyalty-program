package com.yowyob.loyalty.api.wallet.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FreezeRequest(@NotBlank String reason) {}
