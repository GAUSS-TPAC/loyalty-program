package com.yowyob.loyalty.infrastructure.persistence.subscription.repository;

import com.yowyob.loyalty.infrastructure.persistence.subscription.entity.SubscriptionPlanEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SubscriptionPlanR2dbcRepository extends ReactiveCrudRepository<SubscriptionPlanEntity, UUID> {
    Flux<SubscriptionPlanEntity> findAllByActiveTrue();
    Mono<SubscriptionPlanEntity> findByCode(String code);
}
