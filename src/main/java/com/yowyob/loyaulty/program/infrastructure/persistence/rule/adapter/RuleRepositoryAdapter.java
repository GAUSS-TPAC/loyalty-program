package com.yowyob.loyaulty.program.infrastructure.persistence.rule.adapter;

import com.yowyob.loyaulty.program.domain.rule.model.Rule;
import com.yowyob.loyaulty.program.domain.rule.port.out.RuleRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.infrastructure.persistence.rule.mapper.RuleMapper;
import com.yowyob.loyaulty.program.infrastructure.persistence.rule.repository.RuleR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RuleRepositoryAdapter implements RuleRepository {

    private final RuleR2dbcRepository repository;
    private final RuleMapper mapper;

    public RuleRepositoryAdapter(RuleR2dbcRepository repository, RuleMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Rule> save(Rule rule) {
        return repository.save(mapper.toEntity(rule))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Rule> findById(UUID id, TenantId tenantId) {
        return repository.findByIdAndTenantId(id, tenantId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Rule> findActiveByTenant(TenantId tenantId) {
        return repository.findActiveByTenantId(tenantId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Rule> findAllByTenant(TenantId tenantId) {
        return repository.findByTenantId(tenantId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> delete(UUID id, TenantId tenantId) {
        return repository.deleteByIdAndTenantId(id, tenantId.value());
    }
}
