package com.yowyob.loyalty.domain.loyalty.model.event;

import java.util.Map;

public record AppliedEffect(
        String effectType,
        String ruleId,
        String ruleName,
        Map<String, Object> details
) {
}
