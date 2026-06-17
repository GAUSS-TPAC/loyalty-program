package com.yowyob.loyalty.api.loyalty.dto.response;

import com.yowyob.loyalty.domain.loyalty.model.tier.TierPolicy;

import java.math.BigDecimal;
import java.util.List;

public record TierPolicyResponse(
        String tenantId,
        String criterion,
        List<TierThresholdResponse> thresholds,
        String maintainPeriod,
        long maintainThresholdPoints,
        int downgradeGraceDays
) {
    public record TierThresholdResponse(String level, long threshold, BigDecimal multiplier) {}

    public static TierPolicyResponse from(TierPolicy policy) {
        List<TierThresholdResponse> thresholds = policy.thresholds().stream()
                .map(t -> new TierThresholdResponse(t.level().name(), t.threshold(), t.multiplier()))
                .toList();
        return new TierPolicyResponse(
                policy.tenantId().value().toString(),
                policy.criterion(),
                thresholds,
                policy.maintainPeriod(),
                policy.maintainThresholdPoints(),
                policy.downgradeGraceDays()
        );
    }
}
