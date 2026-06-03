package com.yowyob.loyalty.domain.loyalty.model.tier;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MemberTier(
        UUID id,
        TenantId tenantId,
        UserId memberId,
        TierLevel level,
        BigDecimal pointsMultiplier,
        Instant reachedAt,
        Instant validUntil
) {
    public static MemberTier defaultTier(UUID id, TenantId tenantId, UserId memberId) {
        return new MemberTier(id, tenantId, memberId, TierLevel.BRONZE, BigDecimal.ONE, Instant.now(), null);
    }

    public MemberTier withLevel(TierLevel newLevel, BigDecimal multiplier) {
        return new MemberTier(id, tenantId, memberId, newLevel, multiplier, Instant.now(), validUntil);
    }
}
