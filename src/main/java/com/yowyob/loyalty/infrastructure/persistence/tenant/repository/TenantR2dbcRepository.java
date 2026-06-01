package com.yowyob.loyalty.infrastructure.persistence.tenant.repository;

import com.yowyob.loyalty.infrastructure.persistence.tenant.entity.TenantEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TenantR2dbcRepository extends ReactiveCrudRepository<TenantEntity, UUID> {
    Mono<TenantEntity> findBySlug(String slug);
    Flux<TenantEntity> findByStatus(String status);
}
