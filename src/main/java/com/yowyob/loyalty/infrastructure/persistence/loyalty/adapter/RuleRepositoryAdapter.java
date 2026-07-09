package com.yowyob.loyalty.infrastructure.persistence.loyalty.adapter;

import com.yowyob.loyalty.domain.loyalty.model.rule.Rule;
import com.yowyob.loyalty.domain.loyalty.model.rule.RuleStatus;
import com.yowyob.loyalty.domain.loyalty.port.out.RuleRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.entity.RuleEntity;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.mapper.LoyaltyPersistenceMapper;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.repository.RuleR2dbcRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RuleRepositoryAdapter implements RuleRepository {

    private final RuleR2dbcRepository repository;
    private final LoyaltyPersistenceMapper mapper;
    private final R2dbcEntityTemplate template;

    public RuleRepositoryAdapter(RuleR2dbcRepository repository, LoyaltyPersistenceMapper mapper, R2dbcEntityTemplate template) {
        this.repository = repository;
        this.mapper = mapper;
        this.template = template;
    }

    // RuleEntity's id is a client-generated UUID (set before the first save), and it doesn't
    // implement Persistable, so ReactiveCrudRepository.save() can't tell "new" from
    // "existing" and always issues an UPDATE -- which fails with "Row does not exist" for a
    // genuinely new rule. Decide explicitly instead: insert() if the id isn't in the table yet.
    @Override
    public Rule save(Rule rule) {
        RuleEntity entity = mapper.toEntity(rule);
        boolean exists = Boolean.TRUE.equals(repository.existsById(entity.getId()).block());
        RuleEntity saved = exists ? repository.save(entity).block() : template.insert(entity).block();
        return mapper.toDomain(saved);
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
