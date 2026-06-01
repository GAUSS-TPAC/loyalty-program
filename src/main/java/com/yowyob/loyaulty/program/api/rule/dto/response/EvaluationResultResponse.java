package com.yowyob.loyaulty.program.api.rule.dto.response;

import com.yowyob.loyaulty.program.domain.rule.model.EvaluationResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record EvaluationResultResponse(
        UUID eventId,
        String memberId,
        List<AppliedEffectDto> effectsApplied,
        List<String> notifications
) {
    public record AppliedEffectDto(UUID ruleId, String ruleName, String type, Map<String, Object> params) {}

    public static EvaluationResultResponse from(EvaluationResult r) {
        List<AppliedEffectDto> dtos = r.appliedEffects().stream()
                .map(e -> new AppliedEffectDto(e.ruleId(), e.ruleName(), e.type().name(), e.params()))
                .toList();
        return new EvaluationResultResponse(r.eventId(), r.memberId(), dtos, r.notifications());
    }
}
