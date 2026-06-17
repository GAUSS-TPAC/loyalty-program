package com.yowyob.loyalty.domain.tenant.model;

import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.UUID;

public record ApiKey(
        UUID id,
        TenantId tenantId,
        String name,
        String keyHash,
        String keyPrefix,
        boolean active,
        Instant createdAt,
        Instant lastUsedAt
) {
    public static ApiKey create(TenantId tenantId, String name, String keyHash, String keyPrefix) {
        return new ApiKey(UUID.randomUUID(), tenantId, name, keyHash, keyPrefix, true, Instant.now(), null);
    }

    public ApiKey markUsed() {
        return new ApiKey(id, tenantId, name, keyHash, keyPrefix, active, createdAt, Instant.now());
    }

    public ApiKey revoke() {
        return new ApiKey(id, tenantId, name, keyHash, keyPrefix, false, createdAt, lastUsedAt);
    }
}
