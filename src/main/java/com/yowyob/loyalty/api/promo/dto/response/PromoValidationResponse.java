package com.yowyob.loyalty.api.promo.dto.response;

import com.yowyob.loyalty.domain.promo.model.PromoDiscountType;
import com.yowyob.loyalty.domain.promo.model.PromoValidationResult;

import java.math.BigDecimal;
import java.util.UUID;

public record PromoValidationResponse(
        boolean valid,
        UUID campaignId,
        String campaignName,
        PromoDiscountType discountType,
        BigDecimal discountValue,
        BigDecimal calculatedDiscount,
        String message
) {
    public static PromoValidationResponse from(PromoValidationResult r) {
        return new PromoValidationResponse(
                r.isValid(), r.campaignId(), r.campaignName(),
                r.discountType(), r.discountValue(), r.calculatedDiscount(), r.message()
        );
    }
}
