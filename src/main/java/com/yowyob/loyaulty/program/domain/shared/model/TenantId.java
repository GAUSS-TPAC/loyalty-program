package com.yowyob.loyaulty.program.domain.shared.model;

import java.util.Objects;
import java.util.UUID;

public final class TenantId {

    private final UUID value;

    private TenantId(UUID value) {
        this.value = value;
    }

    public static TenantId of(String value) {
        Objects.requireNonNull(value, "TenantId cannot be null");
        try {
            return new TenantId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid TenantId format: " + value, e);
        }
    }

    public static TenantId of(UUID value) {
        Objects.requireNonNull(value, "TenantId cannot be null");
        return new TenantId(value);
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TenantId other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
