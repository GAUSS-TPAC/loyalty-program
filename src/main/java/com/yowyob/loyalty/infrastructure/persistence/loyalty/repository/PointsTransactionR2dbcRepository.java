package com.yowyob.loyalty.infrastructure.persistence.loyalty.repository;

import com.yowyob.loyalty.infrastructure.persistence.loyalty.entity.PointsTransactionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PointsTransactionR2dbcRepository extends ReactiveCrudRepository<PointsTransactionEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndEventIdempotencyKey(UUID tenantId, String eventIdempotencyKey);

    Flux<PointsTransactionEntity> findByPointsAccountIdOrderByCreatedAtDesc(UUID pointsAccountId);
}
