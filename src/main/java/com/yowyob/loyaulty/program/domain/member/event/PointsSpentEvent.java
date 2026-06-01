package com.yowyob.loyaulty.program.domain.member.event;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.shared.port.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record PointsSpentEvent(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        String memberId,
        long amount,
        long newBalance,
        String rewardId
) implements DomainEvent {

    public PointsSpentEvent(TenantId tenantId, String memberId,
                             long amount, long newBalance, String rewardId) {
        this(UUID.randomUUID(), Instant.now(), tenantId, memberId, amount, newBalance, rewardId);
    }

    @Override
    public String eventType() { return "points.spent"; }
}
