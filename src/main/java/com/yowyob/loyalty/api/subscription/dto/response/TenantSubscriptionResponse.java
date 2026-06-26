package com.yowyob.loyalty.api.subscription.dto.response;

import com.yowyob.loyalty.domain.subscription.model.TenantSubscription;

import java.time.Instant;
import java.util.UUID;

public record TenantSubscriptionResponse(
        UUID id,
        UUID tenantId,
        UUID planId,
        String status,
        String billingCycle,
        Instant currentPeriodStart,
        Instant currentPeriodEnd,
        Instant trialEndDate,
        Instant cancelledAt,
        Instant createdAt
) {
    public static TenantSubscriptionResponse from(TenantSubscription s) {
        return new TenantSubscriptionResponse(
                s.id(), s.tenantId().value(), s.planId(),
                s.status().name(), s.billingCycle().name(),
                s.currentPeriodStart(), s.currentPeriodEnd(),
                s.trialEndDate(), s.cancelledAt(), s.createdAt()
        );
    }
}
