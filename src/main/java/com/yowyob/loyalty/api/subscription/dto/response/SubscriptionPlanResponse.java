package com.yowyob.loyalty.api.subscription.dto.response;

import com.yowyob.loyalty.domain.subscription.model.SubscriptionPlan;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SubscriptionPlanResponse(
        UUID id,
        String code,
        String name,
        String description,
        BigDecimal priceMonthly,
        BigDecimal priceYearly,
        String currency,
        PlanFeaturesResponse features,
        boolean active,
        Instant createdAt
) {
    public static SubscriptionPlanResponse from(SubscriptionPlan p) {
        return new SubscriptionPlanResponse(
                p.id(), p.code(), p.name(), p.description(),
                p.priceMonthly(), p.priceYearly(), p.currency(),
                PlanFeaturesResponse.from(p.features()),
                p.active(), p.createdAt()
        );
    }
}
