package com.yowyob.loyalty.domain.referral.event;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReferralConvertedEvent(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        UUID referralEventId,
        UserId referrerId,
        UserId refereeId,
        BigDecimal conversionAmount
) implements ReferralDomainEvent {
    @Override public String eventType() { return "referral.converted"; }
}
