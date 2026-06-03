package com.yowyob.loyalty.domain.loyalty.model.points;

import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record PointsTransaction(
        UUID id,
        UUID pointsAccountId,
        TenantId tenantId,
        String type,
        long amount,
        long balanceAfter,
        String source,
        UUID ruleId,
        String eventIdempotencyKey,
        Map<String, Object> metadata,
        Instant createdAt
) {
    public static PointsTransaction forCredit(UUID accountId, TenantId tenantId, long amount,
                                              long balanceAfter, UUID ruleId, String eventKey) {
        return new PointsTransaction(UUID.randomUUID(), accountId, tenantId, "CREDIT", amount, balanceAfter,
                "RULE_ENGINE", ruleId, eventKey, Map.of(), Instant.now());
    }

    public static PointsTransaction forDebit(UUID accountId, TenantId tenantId, long amount, long balanceAfter) {
        return new PointsTransaction(UUID.randomUUID(), accountId, tenantId, "DEBIT", amount, balanceAfter,
                "REDEMPTION", null, null, Map.of(), Instant.now());
    }
}
