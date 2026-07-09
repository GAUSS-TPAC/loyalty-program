package com.yowyob.loyalty.infrastructure.persistence.loyalty.adapter;

import com.yowyob.loyalty.domain.loyalty.model.counter.Counter;
import com.yowyob.loyalty.domain.loyalty.port.out.CounterRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.entity.CounterEntity;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.mapper.LoyaltyPersistenceMapper;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.repository.CounterR2dbcRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CounterRepositoryAdapter implements CounterRepository {

    private final CounterR2dbcRepository repository;
    private final LoyaltyPersistenceMapper mapper;
    private final R2dbcEntityTemplate template;

    public CounterRepositoryAdapter(CounterR2dbcRepository repository, LoyaltyPersistenceMapper mapper, R2dbcEntityTemplate template) {
        this.repository = repository;
        this.mapper = mapper;
        this.template = template;
    }

    // Client-generated UUID id + no Persistable => save() always issues UPDATE. Decide
    // insert vs update explicitly (see RuleRepositoryAdapter for the full explanation).
    @Override
    public Counter save(Counter counter) {
        CounterEntity entity = mapper.toEntity(counter);
        boolean exists = Boolean.TRUE.equals(repository.existsById(entity.getId()).block());
        CounterEntity saved = exists ? repository.save(entity).block() : template.insert(entity).block();
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Counter> findByKey(TenantId tenantId, UserId memberId, String counterKey) {
        return repository.findByMemberIdAndTenantIdAndCounterKey(memberId.value(), tenantId.value(), counterKey)
                .map(mapper::toDomain)
                .blockOptional();
    }

    @Override
    public List<Counter> findAllByMember(TenantId tenantId, UserId memberId) {
        return repository.findByMemberIdAndTenantId(memberId.value(), tenantId.value())
                .map(mapper::toDomain)
                .collectList()
                .block();
    }
}
