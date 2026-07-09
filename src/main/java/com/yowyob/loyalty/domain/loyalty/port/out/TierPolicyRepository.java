package com.yowyob.loyalty.domain.loyalty.port.out;

import com.yowyob.loyalty.domain.loyalty.model.tier.TierPolicy;
import com.yowyob.loyalty.domain.shared.model.TenantId;

import reactor.core.publisher.Mono;

public interface TierPolicyRepository {
    Mono<TierPolicy> findByTenantId(TenantId tenantId);
    Mono<TierPolicy> save(TierPolicy tierPolicy);
}
