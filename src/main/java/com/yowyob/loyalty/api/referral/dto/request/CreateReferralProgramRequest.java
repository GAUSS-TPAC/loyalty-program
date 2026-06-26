package com.yowyob.loyalty.api.referral.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateReferralProgramRequest(
        @NotBlank String name,
        @Min(0) int maxReferrals,
        @Min(1) int windowDays,
        BigDecimal referrerRewardAmount,
        BigDecimal refereeRewardAmount,
        int minConversionAmount,
        LocalDate startDate,
        LocalDate endDate
) {}
