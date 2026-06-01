package com.yowyob.loyaulty.program.domain.shared.model;

import java.time.Instant;

public record AuditInfo(
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
    public static AuditInfo create(String createdBy) {
        Instant now = Instant.now();
        return new AuditInfo(now, now, createdBy, createdBy);
    }

    public AuditInfo update(String updatedBy) {
        return new AuditInfo(this.createdAt, Instant.now(), this.createdBy, updatedBy);
    }
}
