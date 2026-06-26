package com.yowyob.loyalty.infrastructure.persistence.reward.repository;

import com.yowyob.loyalty.infrastructure.persistence.reward.entity.RewardEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RewardR2dbcRepository extends ReactiveCrudRepository<RewardEntity, UUID> {

    Flux<RewardEntity> findByTenantIdAndStatus(UUID tenantId, String status, Pageable pageable);

    Flux<RewardEntity> findByTenantId(UUID tenantId, Pageable pageable);

    Mono<RewardEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Mono<Boolean> existsByIdAndTenantId(UUID id, UUID tenantId);

    @Modifying
    @Query("UPDATE rewards SET stock_remaining = stock_remaining - 1, " +
           "status = CASE WHEN stock_remaining - 1 = 0 THEN 'EXHAUSTED' ELSE status END, " +
           "version = version + 1, updated_at = NOW() " +
           "WHERE id = :id AND tenant_id = :tenantId AND version = :version " +
           "AND (stock_remaining IS NULL OR stock_remaining > 0)")
    Mono<Integer> decrementStockAtomically(UUID id, UUID tenantId, int version);
}
