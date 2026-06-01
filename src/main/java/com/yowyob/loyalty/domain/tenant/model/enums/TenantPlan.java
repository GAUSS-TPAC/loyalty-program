package com.yowyob.loyalty.domain.tenant.model.enums;

public enum TenantPlan {
    FREE(5),
    PRO(50),
    ENTERPRISE(Integer.MAX_VALUE);

    private final int maxRules;

    TenantPlan(int maxRules) {
        this.maxRules = maxRules;
    }

    public int getMaxRules() {
        return maxRules;
    }
}
