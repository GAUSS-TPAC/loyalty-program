package com.yowyob.loyaulty.program.domain.referral.model;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.UUID;

public record ReferralLink(
        UUID id,
        TenantId tenantId,
        String referrerId,
        String code,
        Instant createdAt
) {
    public static ReferralLink create(TenantId tenantId, String referrerId) {
        String code = "REF-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return new ReferralLink(UUID.randomUUID(), tenantId, referrerId, code, Instant.now());
    }
}
