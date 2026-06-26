package com.yowyob.loyalty.domain.reward.event;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.time.Instant;
import java.util.UUID;

public record RewardGrantExpiredEvent(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        UserId memberId,
        UUID grantId,
        UUID rewardId
) implements RewardDomainEvent {
    @Override
    public String eventType() { return "reward.grant.expired"; }
}
