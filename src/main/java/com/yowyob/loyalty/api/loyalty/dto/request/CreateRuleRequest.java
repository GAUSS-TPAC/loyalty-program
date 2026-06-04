package com.yowyob.loyalty.api.loyalty.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record CreateRuleRequest(
        @NotBlank String name,
        String description,
        @NotNull @Valid TriggerRequest trigger,
        @NotEmpty List<@Valid ConditionRequest> conditions,
        @NotEmpty List<@Valid EffectRequest> effects,
        int priority,
        Instant validFrom,
        Instant validUntil
) {
    public record TriggerRequest(@NotBlank String eventType, Map<String, Object> filters) {}

    public record ConditionRequest(
            @NotBlank String type,
            @NotBlank String operator,
            Object thresholdValue,
            String windowType,
            String counterKey
    ) {}

    public record EffectRequest(@NotBlank String type, Map<String, Object> params) {}
}
