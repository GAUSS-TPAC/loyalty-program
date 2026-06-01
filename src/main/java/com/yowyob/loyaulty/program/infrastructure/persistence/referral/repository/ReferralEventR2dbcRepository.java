package com.yowyob.loyaulty.program.infrastructure.persistence.referral.repository;

import com.yowyob.loyaulty.program.infrastructure.persistence.referral.entity.ReferralEventEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReferralEventR2dbcRepository extends ReactiveCrudRepository<ReferralEventEntity, UUID> {

    Mono<ReferralEventEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("SELECT * FROM referral_events WHERE tenant_id = :tenantId AND referee_id = :refereeId AND status = 'PENDING' LIMIT 1")
    Mono<ReferralEventEntity> findPendingByRefereeId(UUID tenantId, String refereeId);

    Flux<ReferralEventEntity> findByTenantIdAndReferrerId(UUID tenantId, String referrerId);

    Flux<ReferralEventEntity> findByTenantId(UUID tenantId);
}
