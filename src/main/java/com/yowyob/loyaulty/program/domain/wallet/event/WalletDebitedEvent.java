package com.yowyob.loyaulty.program.domain.wallet.event;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.shared.port.DomainEvent;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletDebitedEvent(
        UUID walletId,
        TenantId tenantId,
        String memberId,
        BigDecimal amount,
        BigDecimal newBalance,
        TransactionSource source
) implements DomainEvent {
    @Override public UUID eventId() { return UUID.randomUUID(); }
    @Override public Instant occurredAt() { return Instant.now(); }
    @Override public TenantId tenantId() { return tenantId; }
    @Override public String eventType() { return "wallet.debited"; }
}
