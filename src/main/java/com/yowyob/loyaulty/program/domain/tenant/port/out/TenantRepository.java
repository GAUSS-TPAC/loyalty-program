package com.yowyob.loyaulty.program.domain.tenant.port.out;

import com.yowyob.loyaulty.program.domain.tenant.model.Tenant;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TenantRepository {
    Mono<Tenant> findById(TenantId id);
    Mono<Tenant> findBySlug(String slug);
    Mono<Boolean> existsById(TenantId id);
    Mono<Tenant> save(Tenant tenant);
    Flux<Tenant> findAllActive();
}
