package com.yowyob.loyalty.api.promo.dto.response;

import com.yowyob.loyalty.domain.promo.model.PromoCampaign;
import com.yowyob.loyalty.domain.promo.model.PromoDiscountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PromoCampaignResponse(
        UUID id,
        String code,
        String name,
        PromoDiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minOrderAmount,
        int maxUses,
        int perMemberLimit,
        Instant startDate,
        Instant endDate,
        boolean active,
        Instant createdAt
) {
    public static PromoCampaignResponse from(PromoCampaign c) {
        return new PromoCampaignResponse(
                c.id(), c.code(), c.name(), c.discountType(), c.discountValue(),
                c.minOrderAmount(), c.maxUses(), c.perMemberLimit(),
                c.startDate(), c.endDate(), c.isActive(), c.createdAt()
        );
    }
}
