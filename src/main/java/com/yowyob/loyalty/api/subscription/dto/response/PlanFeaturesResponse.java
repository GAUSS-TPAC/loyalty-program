package com.yowyob.loyalty.api.subscription.dto.response;

import com.yowyob.loyalty.domain.subscription.model.PlanFeatures;

public record PlanFeaturesResponse(
        int maxRules,
        int maxMembers,
        int maxEventsPerMonth,
        boolean referralEnabled,
        boolean campaignsEnabled,
        boolean promoCodesEnabled,
        boolean analyticsEnabled
) {
    public static PlanFeaturesResponse from(PlanFeatures f) {
        return new PlanFeaturesResponse(
                f.maxRules(), f.maxMembers(), f.maxEventsPerMonth(),
                f.referralEnabled(), f.campaignsEnabled(),
                f.promoCodesEnabled(), f.analyticsEnabled()
        );
    }
}
