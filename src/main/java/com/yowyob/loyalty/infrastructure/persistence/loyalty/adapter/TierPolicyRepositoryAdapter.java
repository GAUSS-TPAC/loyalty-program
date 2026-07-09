package com.yowyob.loyalty.infrastructure.persistence.loyalty.adapter;

import com.yowyob.loyalty.domain.loyalty.model.tier.TierPolicy;
import com.yowyob.loyalty.domain.loyalty.port.out.TierPolicyRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.entity.TierPolicyEntity;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.mapper.LoyaltyPersistenceMapper;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.repository.TierPolicyR2dbcRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Component
public class TierPolicyRepositoryAdapter implements TierPolicyRepository {

    private final TierPolicyR2dbcRepository repository;
    private final LoyaltyPersistenceMapper mapper;
    private final R2dbcEntityTemplate template;

    public TierPolicyRepositoryAdapter(TierPolicyR2dbcRepository repository, LoyaltyPersistenceMapper mapper, R2dbcEntityTemplate template) {
        this.repository = repository;
        this.mapper = mapper;
        this.template = template;
    }

    @Override
    public Mono<TierPolicy> findByTenantId(TenantId tenantId) {
        return repository.findByTenantId(tenantId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Mono<TierPolicy> save(TierPolicy tierPolicy) {
        UUID tenantUuid = tierPolicy.tenantId().value();
        return repository.findByTenantId(tenantUuid)
                .flatMap(existing -> {
                    TierPolicyEntity entity = mapper.toEntity(tierPolicy);
                    entity.setId(existing.getId());
                    entity.setCreatedAt(existing.getCreatedAt());
                    entity.setUpdatedAt(Instant.now());
                    return repository.save(entity);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // New policy: client-generated UUID id + no Persistable => save() would
                    // issue an UPDATE (see RuleRepositoryAdapter for the full explanation).
                    TierPolicyEntity entity = mapper.toEntity(tierPolicy);
                    entity.setId(UUID.randomUUID());
                    entity.setCreatedAt(Instant.now());
                    entity.setUpdatedAt(Instant.now());
                    return template.insert(entity);
                }))
                .map(mapper::toDomain);
    }
}
