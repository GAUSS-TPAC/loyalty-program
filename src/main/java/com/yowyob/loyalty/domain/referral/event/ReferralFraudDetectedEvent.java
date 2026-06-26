package com.yowyob.loyalty.domain.referral.event;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.time.Instant;
import java.util.UUID;

public record ReferralFraudDetectedEvent(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        UUID referralEventId,
        UserId suspectedUserId,
        String reason
) implements ReferralDomainEvent {
    @Override public String eventType() { return "referral.fraud.detected"; }
}
