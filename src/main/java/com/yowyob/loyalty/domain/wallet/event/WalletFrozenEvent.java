package com.yowyob.loyalty.domain.wallet.event;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import java.time.Instant;
import java.util.UUID;

public record WalletFrozenEvent(
    UUID eventId,
    TenantId tenantId,
    UUID walletId,
    String reason,
    Instant occurredAt
) implements WalletDomainEvent {
    @Override public String eventType() { return "wallet.frozen"; }
}
