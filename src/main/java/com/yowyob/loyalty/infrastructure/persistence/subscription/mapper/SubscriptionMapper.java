package com.yowyob.loyalty.infrastructure.persistence.subscription.mapper;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.subscription.model.*;
import com.yowyob.loyalty.infrastructure.persistence.subscription.entity.*;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

    // ── Plans ──────────────────────────────────────────────────────────────

    public SubscriptionPlanEntity toEntity(SubscriptionPlan domain) {
        SubscriptionPlanEntity e = new SubscriptionPlanEntity();
        e.setId(domain.id());
        e.setCode(domain.code());
        e.setName(domain.name());
        e.setDescription(domain.description());
        e.setPriceMonthly(domain.priceMonthly());
        e.setPriceYearly(domain.priceYearly());
        e.setCurrency(domain.currency());
        PlanFeatures f = domain.features();
        e.setMaxRules(f.maxRules());
        e.setMaxMembers(f.maxMembers());
        e.setMaxEventsPerMonth(f.maxEventsPerMonth());
        e.setReferralEnabled(f.referralEnabled());
        e.setCampaignsEnabled(f.campaignsEnabled());
        e.setPromoCodesEnabled(f.promoCodesEnabled());
        e.setAnalyticsEnabled(f.analyticsEnabled());
        e.setActive(domain.active());
        e.setCreatedAt(domain.createdAt());
        e.setUpdatedAt(domain.updatedAt());
        return e;
    }

    public SubscriptionPlan toDomain(SubscriptionPlanEntity e) {
        PlanFeatures features = new PlanFeatures(
                e.getMaxRules(), e.getMaxMembers(), e.getMaxEventsPerMonth(),
                e.isReferralEnabled(), e.isCampaignsEnabled(),
                e.isPromoCodesEnabled(), e.isAnalyticsEnabled()
        );
        return SubscriptionPlan.reconstruct(
                e.getId(), e.getCode(), e.getName(), e.getDescription(),
                e.getPriceMonthly(), e.getPriceYearly(), e.getCurrency(),
                features, e.isActive(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    // ── Subscriptions ──────────────────────────────────────────────────────

    public TenantSubscriptionEntity toEntity(TenantSubscription domain) {
        TenantSubscriptionEntity e = new TenantSubscriptionEntity();
        e.setId(domain.id());
        e.setTenantId(domain.tenantId().value());
        e.setPlanId(domain.planId());
        e.setStatus(domain.status().name());
        e.setBillingCycle(domain.billingCycle().name());
        e.setCurrentPeriodStart(domain.currentPeriodStart());
        e.setCurrentPeriodEnd(domain.currentPeriodEnd());
        e.setTrialEndDate(domain.trialEndDate());
        e.setCancelledAt(domain.cancelledAt());
        e.setCreatedAt(domain.createdAt());
        e.setUpdatedAt(domain.updatedAt());
        e.setVersion(domain.version());
        return e;
    }

    public TenantSubscription toDomain(TenantSubscriptionEntity e) {
        return TenantSubscription.reconstruct(
                e.getId(),
                TenantId.of(e.getTenantId()),
                e.getPlanId(),
                SubscriptionStatus.valueOf(e.getStatus()),
                BillingCycle.valueOf(e.getBillingCycle()),
                e.getCurrentPeriodStart(),
                e.getCurrentPeriodEnd(),
                e.getTrialEndDate(),
                e.getCancelledAt(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getVersion()
        );
    }

    // ── Invoices ───────────────────────────────────────────────────────────

    public InvoiceRecordEntity toEntity(InvoiceRecord domain) {
        InvoiceRecordEntity e = new InvoiceRecordEntity();
        e.setId(domain.id());
        e.setTenantId(domain.tenantId().value());
        e.setSubscriptionId(domain.subscriptionId());
        e.setPlanId(domain.planId());
        e.setAmount(domain.amount());
        e.setCurrency(domain.currency());
        e.setStatus(domain.status().name());
        e.setPeriodStart(domain.periodStart());
        e.setPeriodEnd(domain.periodEnd());
        e.setDueDate(domain.dueDate());
        e.setPaidAt(domain.paidAt());
        e.setCreatedAt(domain.createdAt());
        return e;
    }

    public InvoiceRecord toDomain(InvoiceRecordEntity e) {
        return InvoiceRecord.reconstruct(
                e.getId(),
                TenantId.of(e.getTenantId()),
                e.getSubscriptionId(),
                e.getPlanId(),
                e.getAmount(),
                e.getCurrency(),
                InvoiceStatus.valueOf(e.getStatus()),
                e.getPeriodStart(),
                e.getPeriodEnd(),
                e.getDueDate(),
                e.getPaidAt(),
                e.getCreatedAt()
        );
    }
}
