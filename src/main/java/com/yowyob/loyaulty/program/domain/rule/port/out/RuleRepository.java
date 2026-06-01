package com.yowyob.loyaulty.program.domain.rule.port.out;

import com.yowyob.loyaulty.program.domain.rule.model.Rule;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RuleRepository {
    Mono<Rule> save(Rule rule);
    Mono<Rule> findById(UUID id, TenantId tenantId);
    Flux<Rule> findActiveByTenant(TenantId tenantId);
    Flux<Rule> findAllByTenant(TenantId tenantId);
    Mono<Void> delete(UUID id, TenantId tenantId);
}
