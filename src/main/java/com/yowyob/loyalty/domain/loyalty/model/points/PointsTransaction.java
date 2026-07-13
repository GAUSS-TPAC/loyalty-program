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
        return forCredit(accountId, tenantId, amount, balanceAfter, ruleId, eventKey, null);
    }

    public static PointsTransaction forCredit(UUID accountId, TenantId tenantId, long amount,
                                              long balanceAfter, UUID ruleId, String eventKey, UUID apiKeyId) {
        return new PointsTransaction(UUID.randomUUID(), accountId, tenantId, "CREDIT", amount, balanceAfter,
                "RULE_ENGINE", ruleId, eventKey, attributionMetadata(apiKeyId), Instant.now());
    }

    public static PointsTransaction forDebit(UUID accountId, TenantId tenantId, long amount, long balanceAfter) {
        return forDebit(accountId, tenantId, amount, balanceAfter, null);
    }

    public static PointsTransaction forDebit(UUID accountId, TenantId tenantId, long amount,
                                             long balanceAfter, UUID apiKeyId) {
        return new PointsTransaction(UUID.randomUUID(), accountId, tenantId, "DEBIT", amount, balanceAfter,
                "REDEMPTION", null, null, attributionMetadata(apiKeyId), Instant.now());
    }

    private static Map<String, Object> attributionMetadata(UUID apiKeyId) {
        return apiKeyId == null ? Map.of() : Map.of("api_key_id", apiKeyId.toString());
    }

    public static PointsTransaction forManualCredit(UUID accountId, TenantId tenantId, long amount,
                                                     long balanceAfter, String reason) {
        return new PointsTransaction(UUID.randomUUID(), accountId, tenantId, "CREDIT", amount, balanceAfter,
                "MANUAL_ADJUSTMENT", null, null, Map.of("reason", reason), Instant.now());
    }

    public static PointsTransaction forManualDebit(UUID accountId, TenantId tenantId, long amount,
                                                    long balanceAfter, String reason) {
        return new PointsTransaction(UUID.randomUUID(), accountId, tenantId, "DEBIT", amount, balanceAfter,
                "MANUAL_ADJUSTMENT", null, null, Map.of("reason", reason), Instant.now());
    }
}
