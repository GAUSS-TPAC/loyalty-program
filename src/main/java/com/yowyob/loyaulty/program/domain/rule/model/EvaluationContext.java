package com.yowyob.loyaulty.program.domain.rule.model;

import java.util.Map;
import java.util.UUID;

public record EvaluationContext(
        LoyaltyEvent event,
        Map<UUID, Long> countersByRuleId
) {
    public long getCounter(UUID ruleId) {
        return countersByRuleId.getOrDefault(ruleId, 0L);
    }
}
