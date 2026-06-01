package com.yowyob.loyaulty.program.infrastructure.persistence.rule.repository;

import com.yowyob.loyaulty.program.infrastructure.persistence.rule.entity.RuleEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RuleR2dbcRepository extends ReactiveCrudRepository<RuleEntity, UUID> {

    Flux<RuleEntity> findByTenantId(UUID tenantId);

    @Query("SELECT * FROM rules WHERE tenant_id = :tenantId AND status = 'ACTIVE'")
    Flux<RuleEntity> findActiveByTenantId(UUID tenantId);

    Mono<RuleEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Mono<Void> deleteByIdAndTenantId(UUID id, UUID tenantId);
}
