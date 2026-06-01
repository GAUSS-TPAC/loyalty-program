package com.yowyob.loyaulty.program.domain.member.event;

import com.yowyob.loyaulty.program.domain.member.model.enums.TierLevel;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.shared.port.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record TierChangedEvent(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        String memberId,
        TierLevel previousLevel,
        TierLevel newLevel
) implements DomainEvent {

    public TierChangedEvent(TenantId tenantId, String memberId,
                             TierLevel previousLevel, TierLevel newLevel) {
        this(UUID.randomUUID(), Instant.now(), tenantId, memberId, previousLevel, newLevel);
    }

    public boolean isUpgrade() {
        return newLevel.ordinal() > previousLevel.ordinal();
    }

    @Override
    public String eventType() {
        return isUpgrade() ? "member.tier.upgraded" : "member.tier.downgraded";
    }
}
