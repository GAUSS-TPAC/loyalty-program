package com.yowyob.loyalty.api.referral.dto.response;

import com.yowyob.loyalty.domain.referral.model.ReferralEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReferralEventResponse(
        UUID id,
        UUID refereeId,
        String status,
        Instant enrolledAt,
        Instant convertedAt,
        BigDecimal conversionAmount
) {
    public static ReferralEventResponse from(ReferralEvent event) {
        return new ReferralEventResponse(
                event.id(), event.refereeId().value(),
                event.status().name(),
                event.enrolledAt(), event.convertedAt(),
                event.conversionAmount());
    }
}
