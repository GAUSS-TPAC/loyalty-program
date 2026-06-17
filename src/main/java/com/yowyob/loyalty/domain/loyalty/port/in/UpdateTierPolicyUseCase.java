package com.yowyob.loyalty.domain.loyalty.port.in;

import com.yowyob.loyalty.domain.loyalty.model.tier.TierPolicy;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

public interface UpdateTierPolicyUseCase {
    Mono<TierPolicy> getTierPolicy(TenantId tenantId);
    Mono<TierPolicy> upsertTierPolicy(TierPolicy policy);
}
