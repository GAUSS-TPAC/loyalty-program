package com.yowyob.loyalty.infrastructure.persistence.loyalty.repository;

import com.yowyob.loyalty.infrastructure.persistence.loyalty.entity.PointsAccountEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PointsAccountR2dbcRepository extends ReactiveCrudRepository<PointsAccountEntity, UUID> {

    Mono<PointsAccountEntity> findByMemberIdAndTenantId(UUID memberId, UUID tenantId);
}
