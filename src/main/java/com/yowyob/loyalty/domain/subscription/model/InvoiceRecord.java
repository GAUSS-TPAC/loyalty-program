package com.yowyob.loyalty.domain.subscription.model;

import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class InvoiceRecord {

    private final UUID id;
    private final TenantId tenantId;
    private final UUID subscriptionId;
    private final UUID planId;
    private final BigDecimal amount;
    private final String currency;
    private InvoiceStatus status;
    private final Instant periodStart;
    private final Instant periodEnd;
    private final Instant dueDate;
    private Instant paidAt;
    private final Instant createdAt;

    private InvoiceRecord(UUID id, TenantId tenantId, UUID subscriptionId, UUID planId,
                           BigDecimal amount, String currency, InvoiceStatus status,
                           Instant periodStart, Instant periodEnd, Instant dueDate,
                           Instant paidAt, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.subscriptionId = subscriptionId;
        this.planId = planId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.dueDate = dueDate;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
    }

    public static InvoiceRecord generate(TenantId tenantId, UUID subscriptionId, UUID planId,
                                          BigDecimal amount, String currency,
                                          Instant periodStart, Instant periodEnd) {
        Instant now = Instant.now();
        return new InvoiceRecord(
                UUID.randomUUID(), tenantId, subscriptionId, planId,
                amount, currency, InvoiceStatus.PENDING,
                periodStart, periodEnd, now.plus(7, ChronoUnit.DAYS),
                null, now
        );
    }

    public static InvoiceRecord reconstruct(UUID id, TenantId tenantId, UUID subscriptionId, UUID planId,
                                             BigDecimal amount, String currency, InvoiceStatus status,
                                             Instant periodStart, Instant periodEnd, Instant dueDate,
                                             Instant paidAt, Instant createdAt) {
        return new InvoiceRecord(id, tenantId, subscriptionId, planId, amount, currency, status,
                periodStart, periodEnd, dueDate, paidAt, createdAt);
    }

    public void markPaid() {
        this.status = InvoiceStatus.PAID;
        this.paidAt = Instant.now();
    }

    public void markFailed() {
        this.status = InvoiceStatus.FAILED;
    }

    public void markVoid() {
        this.status = InvoiceStatus.VOID;
    }

    public boolean isOverdue(Instant now) {
        return status == InvoiceStatus.PENDING && dueDate.isBefore(now);
    }

    public UUID id()                { return id; }
    public TenantId tenantId()      { return tenantId; }
    public UUID subscriptionId()    { return subscriptionId; }
    public UUID planId()            { return planId; }
    public BigDecimal amount()      { return amount; }
    public String currency()        { return currency; }
    public InvoiceStatus status()   { return status; }
    public Instant periodStart()    { return periodStart; }
    public Instant periodEnd()      { return periodEnd; }
    public Instant dueDate()        { return dueDate; }
    public Instant paidAt()         { return paidAt; }
    public Instant createdAt()      { return createdAt; }
}
