package com.yowyob.loyalty.api.referral.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EnrollReferralRequest(@NotBlank String referralCode) {}
