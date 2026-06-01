package com.yowyob.loyaulty.program.api.referral.dto;

import com.yowyob.loyaulty.program.domain.referral.model.ReferralLink;

import java.time.Instant;
import java.util.UUID;

public record ReferralLinkResponse(
        UUID id,
        String referrerId,
        String code,
        String shareUrl,
        Instant createdAt
) {
    public static ReferralLinkResponse from(ReferralLink link, String baseUrl) {
        return new ReferralLinkResponse(
                link.id(),
                link.referrerId(),
                link.code(),
                baseUrl + "/join?ref=" + link.code(),
                link.createdAt()
        );
    }
}
