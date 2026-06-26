package com.yowyob.loyalty.api.subscription.dto.response;

import com.yowyob.loyalty.domain.subscription.model.InvoiceRecord;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InvoiceRecordResponse(
        UUID id,
        UUID tenantId,
        UUID subscriptionId,
        UUID planId,
        BigDecimal amount,
        String currency,
        String status,
        Instant periodStart,
        Instant periodEnd,
        Instant dueDate,
        Instant paidAt,
        Instant createdAt
) {
    public static InvoiceRecordResponse from(InvoiceRecord inv) {
        return new InvoiceRecordResponse(
                inv.id(), inv.tenantId().value(), inv.subscriptionId(), inv.planId(),
                inv.amount(), inv.currency(), inv.status().name(),
                inv.periodStart(), inv.periodEnd(), inv.dueDate(),
                inv.paidAt(), inv.createdAt()
        );
    }
}
