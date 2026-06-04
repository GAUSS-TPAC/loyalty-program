package com.yowyob.loyalty.infrastructure.persistence.loyalty.adapter;

import com.yowyob.loyalty.domain.loyalty.model.rule.Rule;
import com.yowyob.loyalty.domain.loyalty.model.rule.RuleStatus;
import com.yowyob.loyalty.domain.loyalty.port.out.RuleRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.mapper.LoyaltyPersistenceMapper;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.repository.RuleR2dbcRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RuleRepositoryAdapter implements RuleRepository {

    private final RuleR2dbcRepository repository;
    private final LoyaltyPersistenceMapper mapper;

    public RuleRepositoryAdapter(RuleR2dbcRepository repository, LoyaltyPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Rule save(Rule rule) {
        return mapper.toDomain(repository.save(mapper.toEntity(rule)).block());
    }

    @Override
    public Optional<Rule> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain).blockOptional();
    }

    @Override
    public List<Rule> findActiveRulesByTenantAndEvent(TenantId tenantId, String eventType) {
        return repository.findByTenantIdAndStatus(tenantId.value(), RuleStatus.ACTIVE.name())
                .map(mapper::toDomain)
                .filter(rule -> rule.getTrigger().eventType().equals(eventType))
                .collectList()
                .block();
    }

    @Override
    public List<Rule> findByTenant(TenantId tenantId) {
        return repository.findByTenantId(tenantId.value())
                .map(mapper::toDomain)
                .collectList()
                .block();
    }
}
