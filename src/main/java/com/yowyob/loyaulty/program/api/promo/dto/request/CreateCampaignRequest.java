package com.yowyob.loyaulty.program.api.promo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateCampaignRequest(
        @NotBlank String name,
        String code,
        @NotBlank String discountType,
        @NotNull @Positive BigDecimal discountValue,
        Integer maxUsesTotal,
        Integer maxUsesPerMember,
        BigDecimal minOrderAmount,
        String validFrom,
        String validUntil
) {}
