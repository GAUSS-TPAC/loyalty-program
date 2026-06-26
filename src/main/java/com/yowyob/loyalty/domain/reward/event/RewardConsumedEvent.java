package com.yowyob.loyalty.domain.reward.event;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RewardConsumedEvent(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        UserId memberId,
        UUID grantId,
        UUID rewardId,
        String orderReference,
        BigDecimal discountApplied
) implements RewardDomainEvent {
    @Override
    public String eventType() { return "reward.consumed"; }
}
