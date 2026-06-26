package com.yowyob.loyalty.domain.reward.event;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.time.Instant;
import java.util.UUID;

public record GrantRewardRequestedEvent(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        UserId memberId,
        String rewardId,
        UUID sourceRuleId,
        String sourceEventIdempotencyKey
) implements RewardDomainEvent {
    @Override
    public String eventType() { return "reward.grant.requested"; }
}
