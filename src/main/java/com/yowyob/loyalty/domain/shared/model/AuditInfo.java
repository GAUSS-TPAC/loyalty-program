package com.yowyob.loyalty.domain.shared.model;

import com.yowyob.loyalty.domain.shared.exception.DomainValidationException;
import java.time.Instant;

public record AuditInfo(
    Instant createdAt,
    Instant updatedAt,
    String createdBy,
    String updatedBy
) {
    public AuditInfo {
        if (createdAt == null) {
            throw new DomainValidationException("createdAt ne doit pas être null");
        }
        if (updatedAt == null) {
            throw new DomainValidationException("updatedAt ne doit pas être null");
        }
        if (createdBy == null || createdBy.isBlank()) {
            throw new DomainValidationException("createdBy ne doit pas être vide ou null");
        }
        if (updatedBy == null || updatedBy.isBlank()) {
            throw new DomainValidationException("updatedBy ne doit pas être vide ou null");
        }
    }

    public static AuditInfo now(String createdBy) {
        Instant now = Instant.now();
        return new AuditInfo(now, now, createdBy, createdBy);
    }

    public AuditInfo withUpdate(String updatedBy) {
        return new AuditInfo(this.createdAt, Instant.now(), this.createdBy, updatedBy);
    }
}
