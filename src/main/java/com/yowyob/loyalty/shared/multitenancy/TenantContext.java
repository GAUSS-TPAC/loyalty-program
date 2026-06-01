package com.yowyob.loyalty.shared.multitenancy;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.Tenant;
import com.yowyob.loyalty.domain.tenant.model.enums.TenantPlan;
import com.yowyob.loyalty.domain.tenant.model.enums.TenantStatus;

public record TenantContext(
    TenantId tenantId,
    String tenantName,
    TenantStatus tenantStatus,
    TenantPlan tenantPlan
) {
    public boolean isActive() {
        return tenantStatus.isOperational();
    }

    public static TenantContext from(Tenant tenant) {
        return new TenantContext(
            tenant.getId(),
            tenant.getName(),
            tenant.getStatus(),
            tenant.getPlan()
        );
    }
}
