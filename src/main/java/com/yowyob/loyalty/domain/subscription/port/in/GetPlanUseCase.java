package com.yowyob.loyalty.domain.subscription.port.in;

import com.yowyob.loyalty.domain.subscription.model.SubscriptionPlan;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetPlanUseCase {
    Flux<SubscriptionPlan> listActivePlans();
    Mono<SubscriptionPlan> getPlanById(UUID planId);
    Mono<SubscriptionPlan> getPlanByCode(String code);
}
