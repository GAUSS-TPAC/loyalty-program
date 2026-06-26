package com.yowyob.loyalty.domain.referral.event;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.time.Instant;
import java.util.UUID;

public record ReferralLinkCreatedEvent(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        UUID linkId,
        UserId referrerId,
        String code
) implements ReferralDomainEvent {
    @Override public String eventType() { return "referral.link.created"; }
}
