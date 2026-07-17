package com.yowyob.loyalty.domain.subscription.model;

import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.math.BigDecimal;

/**
 * Vue agrégée d'un tenant abonné au service loyalty, pour la console
 * plateforme (Kernel Core enrichit le nom d'organisation, le reste vient
 * de l'abonnement/facturation locaux).
 */
public record PlatformTenantSummary(
        TenantId tenantId,
        String tenantName,
        TenantSubscription subscription,
        String planCode,
        String planName,
        BigDecimal totalPaidAmount,
        String currency,
        long totalPointsGenerated
) {}
