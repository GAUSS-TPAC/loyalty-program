package com.yowyob.loyalty.domain.webhook.port.out;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.webhook.model.WebhookEndpoint;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface WebhookEndpointRepository {
    Mono<WebhookEndpoint> save(WebhookEndpoint endpoint);
    Mono<WebhookEndpoint> findByIdAndTenantId(UUID id, TenantId tenantId);
    Flux<WebhookEndpoint> findAllByTenantId(TenantId tenantId);
    Flux<WebhookEndpoint> findActiveByTenantId(TenantId tenantId);
    Mono<Void> deleteByIdAndTenantId(UUID id, TenantId tenantId);
}
