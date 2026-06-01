package com.yowyob.loyaulty.program.domain.shared.model;

import java.util.Objects;
import java.util.UUID;

public final class UserId {

    private final UUID value;

    private UserId(UUID value) {
        this.value = value;
    }

    public static UserId of(String value) {
        Objects.requireNonNull(value, "UserId cannot be null");
        try {
            return new UserId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UserId format: " + value, e);
        }
    }

    public static UserId of(UUID value) {
        Objects.requireNonNull(value, "UserId cannot be null");
        return new UserId(value);
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserId other)) return false;
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
