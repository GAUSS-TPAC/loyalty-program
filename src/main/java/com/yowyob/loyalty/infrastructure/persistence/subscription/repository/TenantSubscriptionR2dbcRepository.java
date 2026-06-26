package com.yowyob.loyalty.infrastructure.persistence.subscription.repository;

import com.yowyob.loyalty.infrastructure.persistence.subscription.entity.TenantSubscriptionEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface TenantSubscriptionR2dbcRepository extends ReactiveCrudRepository<TenantSubscriptionEntity, UUID> {
    Mono<TenantSubscriptionEntity> findByTenantId(UUID tenantId);

    @Query("SELECT * FROM tenant_subscriptions WHERE status = 'TRIAL' AND trial_end_date <= :now")
    Flux<TenantSubscriptionEntity> findExpiredTrials(Instant now);

    @Query("SELECT * FROM tenant_subscriptions WHERE status = 'ACTIVE' AND current_period_end <= :now")
    Flux<TenantSubscriptionEntity> findExpiredActive(Instant now);
}
