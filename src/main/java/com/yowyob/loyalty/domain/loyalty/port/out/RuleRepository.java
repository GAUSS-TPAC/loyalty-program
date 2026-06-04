package com.yowyob.loyalty.domain.loyalty.port.out;

import com.yowyob.loyalty.domain.loyalty.model.rule.Rule;
import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RuleRepository {
    Rule save(Rule rule);
    Optional<Rule> findById(UUID id);
    List<Rule> findActiveRulesByTenantAndEvent(TenantId tenantId, String eventType);

    List<Rule> findByTenant(TenantId tenantId);
}
