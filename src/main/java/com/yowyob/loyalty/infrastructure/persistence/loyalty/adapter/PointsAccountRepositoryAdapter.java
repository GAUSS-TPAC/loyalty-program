package com.yowyob.loyalty.infrastructure.persistence.loyalty.adapter;

import com.yowyob.loyalty.domain.loyalty.model.points.PointsAccount;
import com.yowyob.loyalty.domain.loyalty.port.out.PointsAccountRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.entity.PointsAccountEntity;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.mapper.LoyaltyPersistenceMapper;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.repository.PointsAccountR2dbcRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PointsAccountRepositoryAdapter implements PointsAccountRepository {

    private final PointsAccountR2dbcRepository repository;
    private final LoyaltyPersistenceMapper mapper;
    private final R2dbcEntityTemplate template;

    public PointsAccountRepositoryAdapter(PointsAccountR2dbcRepository repository, LoyaltyPersistenceMapper mapper, R2dbcEntityTemplate template) {
        this.repository = repository;
        this.mapper = mapper;
        this.template = template;
    }

    // Client-generated UUID id + no Persistable => save() always issues UPDATE. Decide
    // insert vs update explicitly (see RuleRepositoryAdapter for the full explanation).
    @Override
    public PointsAccount save(PointsAccount account) {
        PointsAccountEntity entity = mapper.toEntity(account);
        boolean exists = Boolean.TRUE.equals(repository.existsById(entity.getId()).block());
        PointsAccountEntity saved = exists ? repository.save(entity).block() : template.insert(entity).block();
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<PointsAccount> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain).blockOptional();
    }

    @Override
    public Optional<PointsAccount> findByMemberId(TenantId tenantId, UserId memberId) {
        return repository.findByMemberIdAndTenantId(memberId.value(), tenantId.value())
                .map(mapper::toDomain)
                .blockOptional();
    }

    @Override
    public reactor.core.publisher.Mono<Long> sumLifetimeEarnedByTenant(TenantId tenantId) {
        return repository.sumLifetimeEarnedByTenantId(tenantId.value());
    }
}
