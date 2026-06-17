package com.yowyob.loyalty.api.tenant.dto;

import com.yowyob.loyalty.domain.tenant.model.ApiKey;

import java.time.Instant;
import java.util.UUID;

public record ApiKeyResponse(
        UUID id,
        String name,
        String keyPrefix,
        boolean active,
        Instant createdAt,
        Instant lastUsedAt,
        String rawKey
) {
    public static ApiKeyResponse from(ApiKey key) {
        return new ApiKeyResponse(key.id(), key.name(), key.keyPrefix() + "...",
                key.active(), key.createdAt(), key.lastUsedAt(), null);
    }

    public static ApiKeyResponse fromCreated(ApiKey key, String rawKey) {
        return new ApiKeyResponse(key.id(), key.name(), key.keyPrefix() + "...",
                key.active(), key.createdAt(), key.lastUsedAt(), rawKey);
    }
}
