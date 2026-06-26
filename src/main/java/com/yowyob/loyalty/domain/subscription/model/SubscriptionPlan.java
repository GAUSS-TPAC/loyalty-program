package com.yowyob.loyalty.domain.subscription.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class SubscriptionPlan {

    private final UUID id;
    private final String code;
    private final String name;
    private final String description;
    private final BigDecimal priceMonthly;
    private final BigDecimal priceYearly;
    private final String currency;
    private final PlanFeatures features;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    private SubscriptionPlan(UUID id, String code, String name, String description,
                              BigDecimal priceMonthly, BigDecimal priceYearly, String currency,
                              PlanFeatures features, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.priceMonthly = priceMonthly;
        this.priceYearly = priceYearly;
        this.currency = currency;
        this.features = features;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SubscriptionPlan create(String code, String name, String description,
                                           BigDecimal priceMonthly, BigDecimal priceYearly, String currency,
                                           PlanFeatures features) {
        if (code == null || code.isBlank()) throw new IllegalArgumentException("Plan code is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Plan name is required");
        Instant now = Instant.now();
        return new SubscriptionPlan(UUID.randomUUID(), code.toUpperCase(), name, description,
                priceMonthly, priceYearly, currency, features, true, now, now);
    }

    public static SubscriptionPlan reconstruct(UUID id, String code, String name, String description,
                                                BigDecimal priceMonthly, BigDecimal priceYearly, String currency,
                                                PlanFeatures features, boolean active,
                                                Instant createdAt, Instant updatedAt) {
        return new SubscriptionPlan(id, code, name, description, priceMonthly, priceYearly,
                currency, features, active, createdAt, updatedAt);
    }

    public BigDecimal priceFor(BillingCycle cycle) {
        return cycle == BillingCycle.YEARLY ? priceYearly : priceMonthly;
    }

    public void activate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public UUID id()              { return id; }
    public String code()          { return code; }
    public String name()          { return name; }
    public String description()   { return description; }
    public BigDecimal priceMonthly() { return priceMonthly; }
    public BigDecimal priceYearly()  { return priceYearly; }
    public String currency()      { return currency; }
    public PlanFeatures features(){ return features; }
    public boolean active()       { return active; }
    public Instant createdAt()    { return createdAt; }
    public Instant updatedAt()    { return updatedAt; }
}
