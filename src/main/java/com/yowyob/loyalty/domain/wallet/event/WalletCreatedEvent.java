package com.yowyob.loyalty.domain.wallet.event;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletCreatedEvent(
    UUID eventId,
    TenantId tenantId,
    UUID walletId,
    UserId memberId,
    Instant occurredAt
) implements WalletDomainEvent {
    @Override public String eventType() { return "wallet.created"; }
}
