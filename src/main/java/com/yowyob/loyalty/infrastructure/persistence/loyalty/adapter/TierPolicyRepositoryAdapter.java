package com.yowyob.loyalty.infrastructure.persistence.loyalty.adapter;

import com.yowyob.loyalty.domain.loyalty.model.tier.TierPolicy;
import com.yowyob.loyalty.domain.loyalty.port.out.TierPolicyRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.mapper.LoyaltyPersistenceMapper;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.repository.TierPolicyR2dbcRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TierPolicyRepositoryAdapter implements TierPolicyRepository {

    private final TierPolicyR2dbcRepository repository;
    private final LoyaltyPersistenceMapper mapper;

    public TierPolicyRepositoryAdapter(TierPolicyR2dbcRepository repository, LoyaltyPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<TierPolicy> findByTenantId(TenantId tenantId) {
        return repository.findByTenantId(tenantId.value())
                .map(mapper::toDomain)
                .blockOptional();
    }
}
