package com.yowyob.loyalty.domain.loyalty.port.in;

import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.EffectDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.Rule;
import com.yowyob.loyalty.domain.loyalty.model.rule.TriggerDefinition;
import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CreateRuleUseCase {
    Rule createRule(
            TenantId tenantId,
            String name,
            String description,
            TriggerDefinition trigger,
            List<ConditionDefinition> conditions,
            List<EffectDefinition> effects,
            int priority,
            Instant validFrom,
            Instant validUntil
    );
}
