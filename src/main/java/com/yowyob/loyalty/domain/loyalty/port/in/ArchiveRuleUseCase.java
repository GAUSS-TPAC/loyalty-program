package com.yowyob.loyalty.domain.loyalty.port.in;

import com.yowyob.loyalty.domain.loyalty.model.rule.Rule;
import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.util.UUID;

public interface ArchiveRuleUseCase {
    Rule archiveRule(TenantId tenantId, UUID ruleId);
}
