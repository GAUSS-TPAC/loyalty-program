package com.yowyob.loyaulty.program.api.rule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record CreateRuleRequest(
        @NotBlank(message = "name is required")
        String name,

        String description,

        int priority,

        @NotNull(message = "trigger is required")
        TriggerDto trigger,

        List<ConditionDto> conditions,

        @NotNull(message = "at least one effect is required")
        List<EffectDto> effects,

        Instant validFrom,
        Instant validUntil
) {
    public record TriggerDto(String eventType, Map<String, String> filters) {}
    public record ConditionDto(String type, String operator, double value, String window) {}
    public record EffectDto(String type, Map<String, Object> params) {}
}
