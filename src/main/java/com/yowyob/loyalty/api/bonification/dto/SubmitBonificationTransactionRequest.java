package com.yowyob.loyalty.api.bonification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SubmitBonificationTransactionRequest(
        @Positive double amount,
        @NotBlank String clientLogin,
        boolean debit
) {}
