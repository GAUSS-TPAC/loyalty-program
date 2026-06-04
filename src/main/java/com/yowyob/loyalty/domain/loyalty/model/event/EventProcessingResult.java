package com.yowyob.loyalty.domain.loyalty.model.event;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.time.Instant;
import java.util.List;

public record EventProcessingResult(
        String eventId,
        TenantId tenantId,
        UserId memberId,
        List<AppliedEffect> effectsApplied,
        List<String> notifications,
        Instant processedAt
) {
    public boolean hasEffects() {
        return effectsApplied != null && !effectsApplied.isEmpty();
    }
}
