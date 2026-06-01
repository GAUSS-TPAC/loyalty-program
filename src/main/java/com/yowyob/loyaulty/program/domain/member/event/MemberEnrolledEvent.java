package com.yowyob.loyaulty.program.domain.member.event;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.shared.port.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record MemberEnrolledEvent(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        String memberId,
        String externalId
) implements DomainEvent {

    public MemberEnrolledEvent(TenantId tenantId, String memberId, String externalId) {
        this(UUID.randomUUID(), Instant.now(), tenantId, memberId, externalId);
    }

    @Override
    public String eventType() { return "member.enrolled"; }
}
