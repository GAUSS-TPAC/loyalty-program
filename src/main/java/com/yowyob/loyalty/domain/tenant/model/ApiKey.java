package com.yowyob.loyalty.domain.tenant.model;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.enums.ApiKeyMode;

import java.time.Instant;
import java.util.UUID;

public record ApiKey(
        UUID id,
        TenantId tenantId,
        String name,
        String keyHash,
        String keyPrefix,
        ApiKeyMode mode,
        boolean active,
        Instant createdAt,
        Instant lastUsedAt,
        UUID ownerId
) {
    public static ApiKey create(TenantId tenantId, String name, String keyHash, String keyPrefix, ApiKeyMode mode, UUID ownerId) {
        return new ApiKey(UUID.randomUUID(), tenantId, name, keyHash, keyPrefix, mode, true, Instant.now(), null, ownerId);
    }

    public ApiKey markUsed() {
        return new ApiKey(id, tenantId, name, keyHash, keyPrefix, mode, active, createdAt, Instant.now(), ownerId);
    }

    public ApiKey revoke() {
        return new ApiKey(id, tenantId, name, keyHash, keyPrefix, mode, false, createdAt, lastUsedAt, ownerId);
    }
}
