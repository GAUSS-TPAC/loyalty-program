package com.yowyob.loyalty.infrastructure.persistence.loyalty.repository;

import com.yowyob.loyalty.infrastructure.persistence.loyalty.entity.TierPolicyEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TierPolicyR2dbcRepository extends ReactiveCrudRepository<TierPolicyEntity, UUID> {

    Mono<TierPolicyEntity> findByTenantId(UUID tenantId);
}
