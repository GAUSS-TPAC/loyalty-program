package com.yowyob.loyalty.infrastructure.persistence.loyalty.adapter;

import com.yowyob.loyalty.domain.loyalty.model.points.PointsAccount;
import com.yowyob.loyalty.domain.loyalty.port.out.PointsAccountRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.mapper.LoyaltyPersistenceMapper;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.repository.PointsAccountR2dbcRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PointsAccountRepositoryAdapter implements PointsAccountRepository {

    private final PointsAccountR2dbcRepository repository;
    private final LoyaltyPersistenceMapper mapper;

    public PointsAccountRepositoryAdapter(PointsAccountR2dbcRepository repository, LoyaltyPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PointsAccount save(PointsAccount account) {
        return mapper.toDomain(repository.save(mapper.toEntity(account)).block());
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
}
