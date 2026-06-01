package com.yowyob.loyalty.domain.shared.model;

import com.yowyob.loyalty.domain.shared.exception.DomainValidationException;
import java.util.UUID;

public record TenantId(UUID value) {
    public TenantId {
        if (value == null) {
            throw new DomainValidationException("value ne doit pas être null");
        }
    }

    public static TenantId of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainValidationException("TenantId invalide : " + raw);
        }
        try {
            return new TenantId(UUID.fromString(raw));
        } catch (IllegalArgumentException e) {
            throw new DomainValidationException("TenantId invalide : " + raw);
        }
    }

    public static TenantId of(UUID uuid) {
        if (uuid == null) {
            throw new DomainValidationException("TenantId invalide : null");
        }
        return new TenantId(uuid);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
