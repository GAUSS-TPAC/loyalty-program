package com.yowyob.loyalty.api.loyalty.dto.response;

import com.yowyob.loyalty.domain.loyalty.model.rule.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RuleResponse(
        UUID id,
        String name,
        String description,
        int priority,
        String status,
        TriggerResponse trigger,
        List<ConditionResponse> conditions,
        List<EffectResponse> effects,
        Instant validFrom,
        Instant validUntil,
        int version,
        Instant createdAt,
        Instant updatedAt
) {
    public record TriggerResponse(String eventType, Map<String, Object> filters) {}

    public record ConditionResponse(
            String type,
            String operator,
            Object thresholdValue,
            String windowType,
            String counterKey
    ) {}

    public record EffectResponse(String type, Map<String, Object> params) {}

    public static RuleResponse from(Rule rule) {
        return new RuleResponse(
                rule.getId(),
                rule.getName(),
                rule.getDescription(),
                rule.getPriority(),
                rule.getStatus().name(),
                new TriggerResponse(rule.getTrigger().eventType(), rule.getTrigger().filters()),
                rule.getConditions().stream()
                        .map(c -> new ConditionResponse(
                                c.type().name(),
                                c.operator().name(),
                                c.thresholdValue(),
                                c.windowType(),
                                c.counterKey()))
                        .toList(),
                rule.getEffects().stream()
                        .map(e -> new EffectResponse(e.type().name(), e.params()))
                        .toList(),
                rule.getValidFrom(),
                rule.getValidUntil(),
                rule.getVersion(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }
}
