package com.yowyob.loyalty.infrastructure.persistence.referral.repository;

import com.yowyob.loyalty.infrastructure.persistence.referral.entity.ReferralEventEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReferralEventR2dbcRepository extends ReactiveCrudRepository<ReferralEventEntity, UUID> {
    Mono<ReferralEventEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("SELECT * FROM referral_events WHERE tenant_id = :tenantId AND referee_id = :refereeId AND status IN ('PENDING', 'ENROLLED') LIMIT 1")
    Mono<ReferralEventEntity> findPendingByRefereeIdAndTenantId(UUID tenantId, UUID refereeId);

    Flux<ReferralEventEntity> findByReferrerIdAndTenantId(UUID referrerId, UUID tenantId);

    Mono<Long> countByReferrerIdAndTenantIdAndStatus(UUID referrerId, UUID tenantId, String status);
}
