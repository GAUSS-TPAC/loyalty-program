package com.yowyob.loyalty.api.loyalty.dto.response;

import com.yowyob.loyalty.domain.loyalty.model.event.AppliedEffect;
import com.yowyob.loyalty.domain.loyalty.model.event.EventProcessingResult;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record EventProcessingResponse(
        String eventId,
        List<AppliedEffectResponse> effectsApplied,
        List<String> notifications,
        Instant processedAt
) {
    public record AppliedEffectResponse(
            String effectType,
            String ruleName,
            Map<String, Object> details
    ) {}

    public static EventProcessingResponse from(EventProcessingResult result) {
        List<AppliedEffectResponse> effects = result.effectsApplied().stream()
                .map(e -> new AppliedEffectResponse(e.effectType(), e.ruleName(), e.details()))
                .toList();
        return new EventProcessingResponse(
                result.eventId(),
                effects,
                result.notifications(),
                result.processedAt()
        );
    }
}
