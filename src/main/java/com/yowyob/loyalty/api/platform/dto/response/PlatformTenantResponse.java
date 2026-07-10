package com.yowyob.loyalty.api.platform.dto.response;

import com.yowyob.loyalty.domain.subscription.model.PlatformTenantSummary;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PlatformTenantResponse(
        UUID tenantId,
        String tenantName,
        String subscriptionStatus,
        String planCode,
        String planName,
        Instant trialEndDate,
        Instant currentPeriodEnd,
        BigDecimal totalPaidAmount,
        String currency
) {
    public static PlatformTenantResponse from(PlatformTenantSummary s) {
        return new PlatformTenantResponse(
                s.tenantId().value(),
                s.tenantName(),
                s.subscription().status().name(),
                s.planCode(),
                s.planName(),
                s.subscription().trialEndDate(),
                s.subscription().currentPeriodEnd(),
                s.totalPaidAmount(),
                s.currency()
        );
    }
}
