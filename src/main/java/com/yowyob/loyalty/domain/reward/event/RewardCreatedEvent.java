package com.yowyob.loyalty.domain.reward.event;

import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.UUID;

public record RewardCreatedEvent(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        UUID rewardId,
        String rewardName
) implements RewardDomainEvent {
    @Override
    public String eventType() { return "reward.created"; }
}
