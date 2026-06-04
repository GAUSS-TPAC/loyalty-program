package com.yowyob.loyalty.infrastructure.persistence.loyalty.repository;

import com.yowyob.loyalty.infrastructure.persistence.loyalty.entity.CounterEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CounterR2dbcRepository extends ReactiveCrudRepository<CounterEntity, UUID> {

    Mono<CounterEntity> findByMemberIdAndTenantIdAndCounterKey(UUID memberId, UUID tenantId, String counterKey);

    Flux<CounterEntity> findByMemberIdAndTenantId(UUID memberId, UUID tenantId);
}
