package com.yowyob.loyalty.api.promo.dto.response;

import com.yowyob.loyalty.domain.promo.model.PromoUsage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PromoApplicationResponse(
        UUID id,
        UUID campaignId,
        String orderId,
        BigDecimal discountApplied,
        Instant usedAt
) {
    public static PromoApplicationResponse from(PromoUsage u) {
        return new PromoApplicationResponse(
                u.id(), u.campaignId(), u.orderId(), u.discountApplied(), u.usedAt()
        );
    }
}
