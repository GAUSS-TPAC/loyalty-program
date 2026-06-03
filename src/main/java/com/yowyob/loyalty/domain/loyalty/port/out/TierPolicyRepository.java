package com.yowyob.loyalty.domain.loyalty.port.out;

import com.yowyob.loyalty.domain.loyalty.model.tier.TierPolicy;
import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.util.Optional;

public interface TierPolicyRepository {
    Optional<TierPolicy> findByTenantId(TenantId tenantId);
}
