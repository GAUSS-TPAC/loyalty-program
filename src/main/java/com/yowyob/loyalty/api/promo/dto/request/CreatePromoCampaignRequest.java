package com.yowyob.loyalty.api.promo.dto.request;

import com.yowyob.loyalty.domain.promo.model.PromoDiscountType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public record CreatePromoCampaignRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull PromoDiscountType discountType,
        @NotNull @Positive BigDecimal discountValue,
        BigDecimal minOrderAmount,
        @Min(0) int maxUses,
        @Min(0) int perMemberLimit,
        @NotNull Instant startDate,
        Instant endDate
) {}
