package com.yowyob.loyalty.api.wallet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmOtpRequest(
    @NotBlank String challengeId,
    @NotBlank @Pattern(regexp = "\\d{6}", message = "Le code OTP doit comporter exactement 6 chiffres") String otpCode,
    String idempotencyKey
) {}
