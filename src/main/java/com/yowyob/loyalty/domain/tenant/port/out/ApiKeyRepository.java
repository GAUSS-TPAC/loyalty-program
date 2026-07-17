package com.yowyob.loyalty.domain.tenant.port.out;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.ApiKey;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ApiKeyRepository {
    Mono<ApiKey> findByKeyHash(String keyHash);
    Flux<ApiKey> findByTenantId(TenantId tenantId);
    Flux<ApiKey> findByTenantIdAndOwnerId(TenantId tenantId, UUID ownerId);
    Mono<ApiKey> save(ApiKey apiKey);
    Mono<Void> deleteById(UUID id);
}
