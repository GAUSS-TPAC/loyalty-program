package com.yowyob.loyalty.api.loyalty;

import com.yowyob.loyalty.api.loyalty.dto.request.CreateRuleRequest;
import com.yowyob.loyalty.api.loyalty.dto.request.IncomingEventRequest;
import com.yowyob.loyalty.domain.loyalty.model.event.IncomingEvent;
import com.yowyob.loyalty.domain.loyalty.model.rule.*;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class LoyaltyApiMapper {

    private LoyaltyApiMapper() {}

    public static IncomingEvent toIncomingEvent(IncomingEventRequest request, TenantId tenantId, String idempotencyKey) {
        return toIncomingEvent(request, tenantId, idempotencyKey, null);
    }

    public static IncomingEvent toIncomingEvent(IncomingEventRequest request, TenantId tenantId,
                                                String idempotencyKey, UUID apiKeyId) {
        return new IncomingEvent(
                request.eventType(),
                tenantId,
                UserId.of(UUID.fromString(request.memberId())),
                idempotencyKey,
                request.occurredAt(),
                request.payload() != null ? request.payload() : Map.of(),
                apiKeyId
        );
    }

    public static TriggerDefinition toTrigger(CreateRuleRequest.TriggerRequest request) {
        return new TriggerDefinition(request.eventType(), request.filters() != null ? request.filters() : Map.of());
    }

    public static List<ConditionDefinition> toConditions(List<CreateRuleRequest.ConditionRequest> conditions) {
        return conditions.stream()
                .map(c -> new ConditionDefinition(
                        ConditionType.valueOf(c.type()),
                        ConditionOperator.valueOf(c.operator()),
                        c.thresholdValue(),
                        c.windowType(),
                        c.counterKey()))
                .toList();
    }

    public static List<EffectDefinition> toEffects(List<CreateRuleRequest.EffectRequest> effects) {
        return effects.stream()
                .map(e -> new EffectDefinition(EffectType.valueOf(e.type()), e.params() != null ? e.params() : Map.of()))
                .toList();
    }
}
