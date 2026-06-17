package com.yowyob.loyalty.application.loyalty.handler;

import com.yowyob.loyalty.domain.loyalty.model.tier.TierPolicy;
import com.yowyob.loyalty.domain.loyalty.port.in.UpdateTierPolicyUseCase;
import com.yowyob.loyalty.domain.loyalty.port.out.TierPolicyRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TierPolicyHandler implements UpdateTierPolicyUseCase {

    private final TierPolicyRepository tierPolicyRepository;

    public TierPolicyHandler(TierPolicyRepository tierPolicyRepository) {
        this.tierPolicyRepository = tierPolicyRepository;
    }

    @Override
    public Mono<TierPolicy> getTierPolicy(TenantId tenantId) {
        return Mono.fromCallable(() ->
                tierPolicyRepository.findByTenantId(tenantId)
                        .orElseGet(() -> TierPolicy.defaults(tenantId)));
    }

    @Override
    public Mono<TierPolicy> upsertTierPolicy(TierPolicy policy) {
        return tierPolicyRepository.save(policy);
    }
}
