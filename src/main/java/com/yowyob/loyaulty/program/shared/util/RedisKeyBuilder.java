package com.yowyob.loyaulty.program.shared.util;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;

public final class RedisKeyBuilder {

    private static final String PREFIX = "loyalty";

    private RedisKeyBuilder() {}

    public static String tenantKey(TenantId tenantId) {
        return PREFIX + ":tenant:" + tenantId.value();
    }

    public static String idempotencyKey(String key) {
        return PREFIX + ":idempotency:" + key;
    }

    public static String walletKey(TenantId tenantId, String walletId) {
        return PREFIX + ":" + tenantId.value() + ":wallet:" + walletId;
    }

    public static String walletBalanceKey(TenantId tenantId, String walletId) {
        return walletKey(tenantId, walletId) + ":balance";
    }

    public static String rateLimitKey(TenantId tenantId, String operation) {
        return PREFIX + ":" + tenantId.value() + ":ratelimit:" + operation;
    }

    public static String counterKey(TenantId tenantId, String memberId, String ruleId) {
        return PREFIX + ":" + tenantId.value() + ":counter:" + memberId + ":" + ruleId;
    }

    public static String rulesCacheKey(TenantId tenantId) {
        return PREFIX + ":" + tenantId.value() + ":rules";
    }

    public static String bonificationAuthTokenKey(TenantId tenantId) {
        return PREFIX + ":bonification:auth:token:" + tenantId.value();
    }

    public static String bonificationTokenKey() {
        return PREFIX + ":bonification:auth:token:global";
    }
}
