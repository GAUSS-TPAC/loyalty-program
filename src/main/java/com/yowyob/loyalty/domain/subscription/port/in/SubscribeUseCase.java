package com.yowyob.loyalty.domain.subscription.port.in;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.subscription.model.BillingCycle;
import com.yowyob.loyalty.domain.subscription.model.TenantSubscription;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SubscribeUseCase {
    Mono<TenantSubscription> startTrial(TenantId tenantId, UUID planId, int trialDays);
    Mono<TenantSubscription> subscribe(TenantId tenantId, UUID planId, BillingCycle cycle);
    Mono<TenantSubscription> changePlan(TenantId tenantId, UUID newPlanId);
    Mono<TenantSubscription> cancelSubscription(TenantId tenantId);
}
