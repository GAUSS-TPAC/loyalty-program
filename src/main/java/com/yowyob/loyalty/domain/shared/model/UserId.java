package com.yowyob.loyalty.domain.shared.model;

import com.yowyob.loyalty.domain.shared.exception.DomainValidationException;
import java.util.UUID;

public record UserId(UUID value) {
    public UserId {
        if (value == null) {
            throw new DomainValidationException("value ne doit pas être null");
        }
    }

    public static UserId of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainValidationException("UserId invalide : " + raw);
        }
        try {
            return new UserId(UUID.fromString(raw));
        } catch (IllegalArgumentException e) {
            throw new DomainValidationException("UserId invalide : " + raw);
        }
    }

    public static UserId of(UUID uuid) {
        if (uuid == null) {
            throw new DomainValidationException("UserId invalide : null");
        }
        return new UserId(uuid);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
