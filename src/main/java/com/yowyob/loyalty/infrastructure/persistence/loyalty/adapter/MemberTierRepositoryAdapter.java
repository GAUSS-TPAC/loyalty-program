package com.yowyob.loyalty.infrastructure.persistence.loyalty.adapter;

import com.yowyob.loyalty.domain.loyalty.model.tier.MemberTier;
import com.yowyob.loyalty.domain.loyalty.port.out.MemberTierRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.entity.MemberTierEntity;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.mapper.LoyaltyPersistenceMapper;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.repository.MemberTierR2dbcRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class MemberTierRepositoryAdapter implements MemberTierRepository {

    private final MemberTierR2dbcRepository repository;
    private final LoyaltyPersistenceMapper mapper;
    private final R2dbcEntityTemplate template;

    public MemberTierRepositoryAdapter(MemberTierR2dbcRepository repository, LoyaltyPersistenceMapper mapper, R2dbcEntityTemplate template) {
        this.repository = repository;
        this.mapper = mapper;
        this.template = template;
    }

    // Client-generated UUID id + no Persistable => save() always issues UPDATE. Decide
    // insert vs update explicitly (see RuleRepositoryAdapter for the full explanation).
    @Override
    public MemberTier save(MemberTier tier) {
        MemberTierEntity entity = mapper.toEntity(tier);
        boolean exists = Boolean.TRUE.equals(repository.existsById(entity.getId()).block());
        MemberTierEntity saved = exists ? repository.save(entity).block() : template.insert(entity).block();
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<MemberTier> findByMemberId(TenantId tenantId, UserId memberId) {
        return repository.findByMemberIdAndTenantId(memberId.value(), tenantId.value())
                .map(mapper::toDomain)
                .blockOptional();
    }

    @Override
    public List<MemberTier> findAllAboveBronze() {
        return repository.findAllByTierLevelNot("BRONZE")
                .map(mapper::toDomain)
                .collectList()
                .block();
    }
}
