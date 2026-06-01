package com.yowyob.loyaulty.program.shared.multitenancy;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.tenant.model.enums.TenantPlan;
import com.yowyob.loyaulty.program.domain.tenant.model.enums.TenantStatus;

public record TenantContext(
        TenantId tenantId,
        String tenantName,
        TenantStatus tenantStatus,
        TenantPlan tenantPlan
) {
    public boolean isActive() {
        return tenantStatus == TenantStatus.ACTIVE;
    }
}
