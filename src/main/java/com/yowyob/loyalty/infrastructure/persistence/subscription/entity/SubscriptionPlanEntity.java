package com.yowyob.loyalty.infrastructure.persistence.subscription.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("subscription_plans")
public class SubscriptionPlanEntity {

    @Id
    private UUID id;
    private String code;
    private String name;
    private String description;
    private BigDecimal priceMonthly;
    private BigDecimal priceYearly;
    private String currency;
    private int maxRules;
    private int maxMembers;
    private int maxEventsPerMonth;
    private boolean referralEnabled;
    private boolean campaignsEnabled;
    private boolean promoCodesEnabled;
    private boolean analyticsEnabled;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPriceMonthly() { return priceMonthly; }
    public void setPriceMonthly(BigDecimal priceMonthly) { this.priceMonthly = priceMonthly; }
    public BigDecimal getPriceYearly() { return priceYearly; }
    public void setPriceYearly(BigDecimal priceYearly) { this.priceYearly = priceYearly; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public int getMaxRules() { return maxRules; }
    public void setMaxRules(int maxRules) { this.maxRules = maxRules; }
    public int getMaxMembers() { return maxMembers; }
    public void setMaxMembers(int maxMembers) { this.maxMembers = maxMembers; }
    public int getMaxEventsPerMonth() { return maxEventsPerMonth; }
    public void setMaxEventsPerMonth(int maxEventsPerMonth) { this.maxEventsPerMonth = maxEventsPerMonth; }
    public boolean isReferralEnabled() { return referralEnabled; }
    public void setReferralEnabled(boolean referralEnabled) { this.referralEnabled = referralEnabled; }
    public boolean isCampaignsEnabled() { return campaignsEnabled; }
    public void setCampaignsEnabled(boolean campaignsEnabled) { this.campaignsEnabled = campaignsEnabled; }
    public boolean isPromoCodesEnabled() { return promoCodesEnabled; }
    public void setPromoCodesEnabled(boolean promoCodesEnabled) { this.promoCodesEnabled = promoCodesEnabled; }
    public boolean isAnalyticsEnabled() { return analyticsEnabled; }
    public void setAnalyticsEnabled(boolean analyticsEnabled) { this.analyticsEnabled = analyticsEnabled; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
