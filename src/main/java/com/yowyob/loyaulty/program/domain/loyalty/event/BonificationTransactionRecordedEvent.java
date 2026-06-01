package com.yowyob.loyaulty.program.domain.loyalty.event;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.shared.port.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record BonificationTransactionRecordedEvent(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        String memberId,
        String transactionId,
        Integer pointsEarned,
        boolean rewardTriggered
) implements DomainEvent {

    @Override
    public String eventType() {
        return "bonification.transaction.recorded";
    }
}
