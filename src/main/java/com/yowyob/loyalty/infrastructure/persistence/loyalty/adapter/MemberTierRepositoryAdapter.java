package com.yowyob.loyalty.infrastructure.persistence.loyalty.adapter;

import com.yowyob.loyalty.domain.loyalty.model.tier.MemberTier;
import com.yowyob.loyalty.domain.loyalty.port.out.MemberTierRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.mapper.LoyaltyPersistenceMapper;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.repository.MemberTierR2dbcRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class MemberTierRepositoryAdapter implements MemberTierRepository {

    private final MemberTierR2dbcRepository repository;
    private final LoyaltyPersistenceMapper mapper;

    public MemberTierRepositoryAdapter(MemberTierR2dbcRepository repository, LoyaltyPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public MemberTier save(MemberTier tier) {
        return mapper.toDomain(repository.save(mapper.toEntity(tier)).block());
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
