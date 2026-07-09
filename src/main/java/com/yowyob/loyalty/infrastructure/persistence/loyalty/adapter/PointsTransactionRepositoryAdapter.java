package com.yowyob.loyalty.infrastructure.persistence.loyalty.adapter;

import com.yowyob.loyalty.domain.loyalty.model.points.PointsTransaction;
import com.yowyob.loyalty.domain.loyalty.port.out.PointsTransactionRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.mapper.LoyaltyPersistenceMapper;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.repository.PointsTransactionR2dbcRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PointsTransactionRepositoryAdapter implements PointsTransactionRepository {

    private final PointsTransactionR2dbcRepository repository;
    private final LoyaltyPersistenceMapper mapper;
    private final R2dbcEntityTemplate template;

    public PointsTransactionRepositoryAdapter(PointsTransactionR2dbcRepository repository, LoyaltyPersistenceMapper mapper, R2dbcEntityTemplate template) {
        this.repository = repository;
        this.mapper = mapper;
        this.template = template;
    }

    // Points transactions are an append-only ledger (never updated), and the id is a
    // client-generated UUID, so save() must always INSERT (see RuleRepositoryAdapter for
    // why ReactiveCrudRepository.save() can't be trusted to do that on its own).
    @Override
    public PointsTransaction save(PointsTransaction tx) {
        return mapper.toDomain(template.insert(mapper.toEntity(tx)).block());
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

    @Override
    public List<PointsTransaction> findByTenantId(TenantId tenantId, int page, int size) {
        return repository.findByTenantIdOrderByCreatedAtDesc(tenantId.value(), PageRequest.of(page, size))
                .map(mapper::toDomain)
                .collectList()
                .block();
    }
}
