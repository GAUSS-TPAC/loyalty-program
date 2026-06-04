package com.yowyob.loyalty.infrastructure.persistence.loyalty.repository;

import com.yowyob.loyalty.infrastructure.persistence.loyalty.entity.RuleEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RuleR2dbcRepository extends ReactiveCrudRepository<RuleEntity, UUID> {

    Flux<RuleEntity> findByTenantIdAndStatus(UUID tenantId, String status);

    Flux<RuleEntity> findByTenantId(UUID tenantId);
}
