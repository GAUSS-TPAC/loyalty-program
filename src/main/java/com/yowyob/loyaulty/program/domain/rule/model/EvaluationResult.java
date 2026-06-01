package com.yowyob.loyaulty.program.domain.rule.model;

import java.util.List;
import java.util.UUID;

public record EvaluationResult(
        UUID eventId,
        String memberId,
        List<AppliedEffect> appliedEffects,
        List<UUID> incrementedCounters,
        List<String> notifications
) {
    public boolean hasEffects() {
        return appliedEffects != null && !appliedEffects.isEmpty();
    }

    public static EvaluationResult empty(UUID eventId, String memberId) {
        return new EvaluationResult(eventId, memberId, List.of(), List.of(), List.of());
    }
}
