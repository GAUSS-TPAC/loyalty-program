package com.yowyob.loyalty.domain.subscription.model;

public record PlanFeatures(
        int maxRules,
        int maxMembers,
        int maxEventsPerMonth,
        boolean referralEnabled,
        boolean campaignsEnabled,
        boolean promoCodesEnabled,
        boolean analyticsEnabled
) {
    public boolean isUnlimitedMembers() { return maxMembers == 0; }
    public boolean isUnlimitedEvents()  { return maxEventsPerMonth == 0; }
    public boolean isUnlimitedRules()   { return maxRules < 0; }
}
