package com.yowyob.loyalty.domain.shared.port;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID eventId();
    Instant occurredAt();
    TenantId tenantId();
    String eventType();
}
