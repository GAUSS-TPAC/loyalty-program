package com.yowyob.loyalty.domain.subscription.port.in;

import com.yowyob.loyalty.domain.subscription.model.PlanFeatures;
import com.yowyob.loyalty.domain.subscription.model.SubscriptionPlan;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface ManagePlanUseCase {
    Mono<SubscriptionPlan> createPlan(String code, String name, String description,
                                       BigDecimal priceMonthly, BigDecimal priceYearly,
                                       String currency, PlanFeatures features);
    Mono<SubscriptionPlan> activatePlan(UUID planId);
    Mono<SubscriptionPlan> deactivatePlan(UUID planId);
}
