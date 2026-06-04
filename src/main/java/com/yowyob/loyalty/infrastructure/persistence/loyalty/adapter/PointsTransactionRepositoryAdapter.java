package com.yowyob.loyalty.infrastructure.persistence.loyalty.adapter;

import com.yowyob.loyalty.domain.loyalty.model.points.PointsTransaction;
import com.yowyob.loyalty.domain.loyalty.port.out.PointsTransactionRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.mapper.LoyaltyPersistenceMapper;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.repository.PointsTransactionR2dbcRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PointsTransactionRepositoryAdapter implements PointsTransactionRepository {

    private final PointsTransactionR2dbcRepository repository;
    private final LoyaltyPersistenceMapper mapper;

    public PointsTransactionRepositoryAdapter(PointsTransactionR2dbcRepository repository, LoyaltyPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PointsTransaction save(PointsTransaction tx) {
        return mapper.toDomain(repository.save(mapper.toEntity(tx)).block());
    }

    @Override
    public List<PointsTransaction> findByAccountId(UUID accountId, int limit, int offset) {
        return repository.findByPointsAccountIdOrderByCreatedAtDesc(accountId)
                .skip(offset)
                .take(limit)
                .map(mapper::toDomain)
                .collectList()
                .block();
    }

    @Override
    public boolean existsByEventIdempotencyKey(TenantId tenantId, String idempotencyKey) {
        if (idempotencyKey == null) {
            return false;
        }
        return Boolean.TRUE.equals(repository.existsByTenantIdAndEventIdempotencyKey(tenantId.value(), idempotencyKey).block());
    }
}
