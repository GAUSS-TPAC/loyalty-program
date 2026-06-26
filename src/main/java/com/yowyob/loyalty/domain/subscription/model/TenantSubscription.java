package com.yowyob.loyalty.domain.subscription.model;

import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class TenantSubscription {

    private final UUID id;
    private final TenantId tenantId;
    private UUID planId;
    private SubscriptionStatus status;
    private final BillingCycle billingCycle;
    private Instant currentPeriodStart;
    private Instant currentPeriodEnd;
    private final Instant trialEndDate;
    private Instant cancelledAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private final long version;

    private TenantSubscription(UUID id, TenantId tenantId, UUID planId, SubscriptionStatus status,
                                BillingCycle billingCycle, Instant currentPeriodStart, Instant currentPeriodEnd,
                                Instant trialEndDate, Instant cancelledAt,
                                Instant createdAt, Instant updatedAt, long version) {
        this.id = id;
        this.tenantId = tenantId;
        this.planId = planId;
        this.status = status;
        this.billingCycle = billingCycle;
        this.currentPeriodStart = currentPeriodStart;
        this.currentPeriodEnd = currentPeriodEnd;
        this.trialEndDate = trialEndDate;
        this.cancelledAt = cancelledAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public static TenantSubscription createTrial(TenantId tenantId, UUID planId, int trialDays) {
        Instant now = Instant.now();
        Instant trialEnd = now.plus(trialDays, ChronoUnit.DAYS);
        return new TenantSubscription(
                UUID.randomUUID(), tenantId, planId, SubscriptionStatus.TRIAL, BillingCycle.MONTHLY,
                now, trialEnd, trialEnd, null, now, now, 0L
        );
    }

    public static TenantSubscription createActive(TenantId tenantId, UUID planId, BillingCycle cycle) {
        Instant now = Instant.now();
        Instant periodEnd = cycle == BillingCycle.YEARLY
                ? now.plus(365, ChronoUnit.DAYS)
                : now.plus(30, ChronoUnit.DAYS);
        return new TenantSubscription(
                UUID.randomUUID(), tenantId, planId, SubscriptionStatus.ACTIVE, cycle,
                now, periodEnd, null, null, now, now, 0L
        );
    }

    public static TenantSubscription reconstruct(UUID id, TenantId tenantId, UUID planId,
                                                  SubscriptionStatus status, BillingCycle billingCycle,
                                                  Instant currentPeriodStart, Instant currentPeriodEnd,
                                                  Instant trialEndDate, Instant cancelledAt,
                                                  Instant createdAt, Instant updatedAt, long version) {
        return new TenantSubscription(id, tenantId, planId, status, billingCycle,
                currentPeriodStart, currentPeriodEnd, trialEndDate, cancelledAt, createdAt, updatedAt, version);
    }

    public boolean isExpired(Instant now) {
        return currentPeriodEnd.isBefore(now);
    }

    public boolean isTrialExpired(Instant now) {
        return trialEndDate != null && trialEndDate.isBefore(now);
    }

    public void renew() {
        Instant now = Instant.now();
        this.currentPeriodStart = now;
        this.currentPeriodEnd = billingCycle == BillingCycle.YEARLY
                ? now.plus(365, ChronoUnit.DAYS)
                : now.plus(30, ChronoUnit.DAYS);
        this.status = SubscriptionStatus.ACTIVE;
        this.updatedAt = now;
    }

    public void cancel() {
        if (status.isTerminal()) throw new IllegalStateException("Subscription already terminal");
        this.status = SubscriptionStatus.CANCELLED;
        this.cancelledAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void markExpired() {
        this.status = SubscriptionStatus.EXPIRED;
        this.updatedAt = Instant.now();
    }

    public void markPastDue() {
        this.status = SubscriptionStatus.PAST_DUE;
        this.updatedAt = Instant.now();
    }

    public void changePlan(UUID newPlanId) {
        this.planId = newPlanId;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = SubscriptionStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public UUID id()                        { return id; }
    public TenantId tenantId()              { return tenantId; }
    public UUID planId()                    { return planId; }
    public SubscriptionStatus status()      { return status; }
    public BillingCycle billingCycle()      { return billingCycle; }
    public Instant currentPeriodStart()     { return currentPeriodStart; }
    public Instant currentPeriodEnd()       { return currentPeriodEnd; }
    public Instant trialEndDate()           { return trialEndDate; }
    public Instant cancelledAt()            { return cancelledAt; }
    public Instant createdAt()              { return createdAt; }
    public Instant updatedAt()              { return updatedAt; }
    public long version()                   { return version; }
}
