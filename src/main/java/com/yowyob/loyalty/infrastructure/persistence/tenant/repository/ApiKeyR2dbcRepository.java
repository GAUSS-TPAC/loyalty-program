package com.yowyob.loyalty.infrastructure.persistence.tenant.repository;

import com.yowyob.loyalty.infrastructure.persistence.tenant.entity.ApiKeyEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ApiKeyR2dbcRepository extends R2dbcRepository<ApiKeyEntity, UUID> {
    Mono<ApiKeyEntity> findByKeyHashAndActiveTrue(String keyHash);
    Flux<ApiKeyEntity> findByTenantId(UUID tenantId);
}
