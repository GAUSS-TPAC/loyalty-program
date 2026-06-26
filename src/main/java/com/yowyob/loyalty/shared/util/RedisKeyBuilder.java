package com.yowyob.loyalty.shared.util;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import java.util.UUID;

public final class RedisKeyBuilder {

    private static final String PREFIX = "loyalty";

    private RedisKeyBuilder() {
        // Prevent instantiation
    }

    public static String tenantKey(TenantId id) {
        return PREFIX + ":tenant:" + id.value();
    }

    public static String tenantCacheKey(TenantId id) {
        return PREFIX + ":tenant:cache:" + id.value();
    }

    public static String idempotencyKey(String key) {
        return PREFIX + ":idempotency:" + key;
    }

    public static String walletKey(TenantId tenantId, UUID walletId) {
        return PREFIX + ":" + tenantId.value() + ":wallet:" + walletId;
    }

    public static String walletBalanceKey(TenantId tenantId, com.yowyob.loyalty.domain.shared.model.UserId memberId) {
        return PREFIX + ":" + tenantId.value() + ":wallet:" + memberId.value() + ":balance";
    }

    public static String rateLimitKey(TenantId tenantId, String operation) {
        return PREFIX + ":" + tenantId.value() + ":ratelimit:" + operation;
    }

    public static String bonificationTokenKey(TenantId tenantId) {
        return PREFIX + ":" + tenantId.value() + ":bonification:auth:token";
    }

    public static String otpChallengeKey(String challengeId) {
        return PREFIX + ":otp:challenge:" + challengeId;
    }

    public static String rewardCatalogKey(TenantId tenantId) {
        return PREFIX + ":" + tenantId.value() + ":rewards:catalog";
    }

    public static String rewardKey(TenantId tenantId, UUID rewardId) {
        return PREFIX + ":" + tenantId.value() + ":reward:" + rewardId;
    }

    public static String rulesKey(TenantId tenantId, String eventType) {
        return PREFIX + ":" + tenantId.value() + ":rules:active:" + eventType;
    }

    public static String counterKey(TenantId tenantId, com.yowyob.loyalty.domain.shared.model.UserId memberId, String counterKey) {
        return PREFIX + ":" + tenantId.value() + ":counter:" + memberId.value() + ":" + counterKey;
    }

    public static String promoCampaignCounterKey(TenantId tenantId, java.util.UUID campaignId) {
        return PREFIX + ":" + tenantId.value() + ":promo:counter:" + campaignId;
    }
}
