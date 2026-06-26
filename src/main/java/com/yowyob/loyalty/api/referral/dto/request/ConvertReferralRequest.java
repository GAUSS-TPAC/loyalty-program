package com.yowyob.loyalty.api.referral.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ConvertReferralRequest(@NotNull @Positive BigDecimal conversionAmount) {}
