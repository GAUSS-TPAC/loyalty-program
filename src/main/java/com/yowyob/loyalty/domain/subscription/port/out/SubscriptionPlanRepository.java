package com.yowyob.loyalty.domain.subscription.port.out;

import com.yowyob.loyalty.domain.subscription.model.SubscriptionPlan;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SubscriptionPlanRepository {
    Mono<SubscriptionPlan> save(SubscriptionPlan plan);
    Mono<SubscriptionPlan> findById(UUID id);
    Mono<SubscriptionPlan> findByCode(String code);
    Flux<SubscriptionPlan> findAllActive();
    Flux<SubscriptionPlan> findAll();
}
