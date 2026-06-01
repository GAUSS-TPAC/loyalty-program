package com.yowyob.loyaulty.program.domain.member.event;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.shared.port.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record PointsEarnedEvent(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        String memberId,
        long amount,
        long newBalance,
        String description
) implements DomainEvent {

    public PointsEarnedEvent(TenantId tenantId, String memberId,
                              long amount, long newBalance, String description) {
        this(UUID.randomUUID(), Instant.now(), tenantId, memberId, amount, newBalance, description);
    }

    @Override
    public String eventType() { return "points.earned"; }
}
