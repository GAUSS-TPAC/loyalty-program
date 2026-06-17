package com.yowyob.loyalty.api.wallet.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateWalletRequest(
        @NotBlank String currencyCode,
        boolean autoActivate
) {}
