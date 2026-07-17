package com.yowyob.loyalty.infrastructure.persistence.tenant.repository;

import com.yowyob.loyalty.infrastructure.persistence.tenant.entity.IntegrationApplicationEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface IntegrationApplicationR2dbcRepository extends R2dbcRepository<IntegrationApplicationEntity, UUID> {
    Flux<IntegrationApplicationEntity> findByTenantId(UUID tenantId);
    Mono<IntegrationApplicationEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    Mono<IntegrationApplicationEntity> findByPublicKey(String publicKey);
    Mono<IntegrationApplicationEntity> findByWebhookEndpointId(UUID webhookEndpointId);
    Mono<Void> deleteByIdAndTenantId(UUID id, UUID tenantId);
}
