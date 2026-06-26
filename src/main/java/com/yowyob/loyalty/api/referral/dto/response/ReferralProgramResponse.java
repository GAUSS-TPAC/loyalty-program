package com.yowyob.loyalty.api.referral.dto.response;

import com.yowyob.loyalty.domain.referral.model.ReferralProgram;

import java.time.Instant;
import java.util.UUID;

public record ReferralProgramResponse(
        UUID id,
        String name,
        boolean active,
        int maxReferralsPerReferrer,
        int referralWindowDays,
        UUID referrerRewardId,
        UUID refereeRewardId,
        int minConversionAmount,
        Instant createdAt
) {
    public static ReferralProgramResponse from(ReferralProgram p) {
        return new ReferralProgramResponse(
                p.id(), p.name(), p.isActive(),
                p.maxReferralsPerReferrer(), p.referralWindowDays(),
                p.referrerRewardId(), p.refereeRewardId(),
                p.minConversionAmount(), p.createdAt());
    }
}
