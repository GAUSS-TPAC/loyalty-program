package com.yowyob.loyaulty.program.domain.wallet.event;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.shared.port.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record WalletClosedEvent(UUID walletId, TenantId tenantId, String memberId)
        implements DomainEvent {
    @Override public UUID eventId() { return UUID.randomUUID(); }
    @Override public Instant occurredAt() { return Instant.now(); }
    @Override public TenantId tenantId() { return tenantId; }
    @Override public String eventType() { return "wallet.closed"; }
}
