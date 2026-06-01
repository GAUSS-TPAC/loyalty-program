package com.yowyob.loyalty.domain.tenant.port.out;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.Tenant;
import reactor.core.publisher.Mono;

public interface TenantRepository {
    Mono<Tenant> findById(TenantId id);
    Mono<Tenant> findBySlug(String slug);
    Mono<Boolean> existsById(TenantId id);
    Mono<Tenant> save(Tenant tenant);
}
