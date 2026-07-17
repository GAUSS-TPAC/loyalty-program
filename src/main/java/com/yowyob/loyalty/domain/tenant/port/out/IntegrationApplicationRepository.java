package com.yowyob.loyalty.domain.tenant.port.out;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.IntegrationApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface IntegrationApplicationRepository {

    Mono<IntegrationApplication> save(IntegrationApplication application);

    Flux<IntegrationApplication> findAllByTenantId(TenantId tenantId);

    Mono<IntegrationApplication> findByIdAndTenantId(UUID id, TenantId tenantId);

    Mono<IntegrationApplication> findByPublicKey(String publicKey);

    Mono<IntegrationApplication> findByWebhookEndpointId(UUID webhookEndpointId);

    Mono<Void> deleteByIdAndTenantId(UUID id, TenantId tenantId);
}
