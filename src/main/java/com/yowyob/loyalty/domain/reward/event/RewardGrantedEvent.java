package com.yowyob.loyalty.domain.reward.event;

import com.yowyob.loyalty.domain.reward.model.GrantSource;
import com.yowyob.loyalty.domain.reward.model.RewardType;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.time.Instant;
import java.util.UUID;

public record RewardGrantedEvent(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        UserId memberId,
        UUID grantId,
        UUID rewardId,
        String rewardName,
        RewardType rewardType,
        GrantSource source
) implements RewardDomainEvent {
    @Override
    public String eventType() { return "reward.granted"; }
}
