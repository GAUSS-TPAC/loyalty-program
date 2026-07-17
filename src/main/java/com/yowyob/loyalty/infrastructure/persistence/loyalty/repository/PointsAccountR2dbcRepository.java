package com.yowyob.loyalty.infrastructure.persistence.loyalty.repository;

import com.yowyob.loyalty.infrastructure.persistence.loyalty.entity.PointsAccountEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PointsAccountR2dbcRepository extends ReactiveCrudRepository<PointsAccountEntity, UUID> {

    Mono<PointsAccountEntity> findByMemberIdAndTenantId(UUID memberId, UUID tenantId);

    @Query("SELECT COALESCE(SUM(lifetime_earned), 0) FROM points_accounts WHERE tenant_id = :tenantId")
    Mono<Long> sumLifetimeEarnedByTenantId(UUID tenantId);
}
