package com.yowyob.loyalty.domain.wallet.event;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletDebitedEvent(
    UUID eventId,
    TenantId tenantId,
    UUID walletId,
    BigDecimal amount,
    BigDecimal newBalance,
    Instant occurredAt
) implements WalletDomainEvent {
    @Override public String eventType() { return "wallet.debited"; }
}
