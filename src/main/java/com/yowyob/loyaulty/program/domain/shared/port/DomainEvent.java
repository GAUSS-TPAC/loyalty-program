package com.yowyob.loyaulty.program.domain.shared.port;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID eventId();
    Instant occurredAt();
    TenantId tenantId();
    String eventType();
}
