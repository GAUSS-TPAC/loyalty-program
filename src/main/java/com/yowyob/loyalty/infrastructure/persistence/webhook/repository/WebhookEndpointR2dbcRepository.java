package com.yowyob.loyalty.infrastructure.persistence.webhook.repository;

import com.yowyob.loyalty.infrastructure.persistence.webhook.entity.WebhookEndpointEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface WebhookEndpointR2dbcRepository extends R2dbcRepository<WebhookEndpointEntity, UUID> {
    Mono<WebhookEndpointEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    Flux<WebhookEndpointEntity> findByTenantId(UUID tenantId);
    Flux<WebhookEndpointEntity> findByTenantIdAndActiveTrue(UUID tenantId);
    Mono<Void> deleteByIdAndTenantId(UUID id, UUID tenantId);
}
