package com.yowyob.loyalty.infrastructure.persistence.loyalty.adapter;

import com.yowyob.loyalty.domain.loyalty.model.counter.Counter;
import com.yowyob.loyalty.domain.loyalty.port.out.CounterRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.mapper.LoyaltyPersistenceMapper;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.repository.CounterR2dbcRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CounterRepositoryAdapter implements CounterRepository {

    private final CounterR2dbcRepository repository;
    private final LoyaltyPersistenceMapper mapper;

    public CounterRepositoryAdapter(CounterR2dbcRepository repository, LoyaltyPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Counter save(Counter counter) {
        return mapper.toDomain(repository.save(mapper.toEntity(counter)).block());
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
