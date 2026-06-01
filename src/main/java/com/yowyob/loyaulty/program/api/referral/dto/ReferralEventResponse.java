package com.yowyob.loyaulty.program.api.referral.dto;

import com.yowyob.loyaulty.program.domain.referral.model.ReferralEvent;

import java.time.Instant;
import java.util.UUID;

public record ReferralEventResponse(
        UUID id,
        String referrerId,
        String refereeId,
        String referralCode,
        String status,
        Instant enrolledAt,
        Instant convertedAt,
        Instant rewardedAt,
        Instant expiresAt
) {
    public static ReferralEventResponse from(ReferralEvent e) {
        return new ReferralEventResponse(
                e.getId(), e.getReferrerId(), e.getRefereeId(),
                e.getReferralCode(), e.getStatus().name(),
                e.getEnrolledAt(), e.getConvertedAt(),
                e.getRewardedAt(), e.getExpiresAt()
        );
    }
}
