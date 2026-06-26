package com.yowyob.loyalty.api.referral.dto.response;

import com.yowyob.loyalty.domain.referral.model.ReferralLink;

import java.time.Instant;
import java.util.UUID;

public record ReferralLinkResponse(
        UUID id,
        String code,
        int usageCount,
        int conversionCount,
        Instant createdAt,
        Instant expiresAt,
        boolean active
) {
    public static ReferralLinkResponse from(ReferralLink link) {
        return new ReferralLinkResponse(
                link.id(), link.code(),
                link.usageCount(), link.conversionCount(),
                link.createdAt(), link.expiresAt(), link.isActive());
    }
}
