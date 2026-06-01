package com.yowyob.loyaulty.program.domain.loyalty.event;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.shared.port.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record BonificationRewardTriggeredEvent(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        String memberId,
        String rewardId,
        String rewardName
) implements DomainEvent {

    @Override
    public String eventType() {
        return "bonification.reward.triggered";
    }
}
