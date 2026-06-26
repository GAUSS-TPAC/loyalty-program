package com.yowyob.loyalty.infrastructure.persistence.reward.repository;

import com.yowyob.loyalty.infrastructure.persistence.reward.entity.RewardGrantEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface RewardGrantR2dbcRepository extends ReactiveCrudRepository<RewardGrantEntity, UUID> {

    Mono<RewardGrantEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Mono<RewardGrantEntity> findByIdempotencyKey(String key);

    Flux<RewardGrantEntity> findByMemberIdAndTenantIdAndStatus(UUID memberId, UUID tenantId, String status);

    Flux<RewardGrantEntity> findByMemberIdAndTenantId(UUID memberId, UUID tenantId, Pageable pageable);

    @Query("SELECT * FROM reward_grants WHERE status = 'ACTIVE' AND expires_at IS NOT NULL AND expires_at < :now LIMIT 500")
    Flux<RewardGrantEntity> findExpiredActiveGrants(Instant now);
}
