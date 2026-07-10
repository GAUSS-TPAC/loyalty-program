package com.yowyob.loyalty.domain.subscription.port.out;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.subscription.model.TenantSubscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface TenantSubscriptionRepository {
    Mono<TenantSubscription> save(TenantSubscription subscription);
    Mono<TenantSubscription> findByTenantId(TenantId tenantId);
    Mono<TenantSubscription> findById(UUID id);
    Flux<TenantSubscription> findExpiredTrials(Instant now);
    Flux<TenantSubscription> findExpiredActive(Instant now);
    Flux<TenantSubscription> findAll();
}
