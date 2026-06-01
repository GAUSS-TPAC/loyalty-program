package com.yowyob.loyaulty.program.shared.exception;

public class TenantSuspendedException extends AppException {
    public TenantSuspendedException(String tenantId) {
        super(ErrorCode.TENANT_SUSPENDED, "Tenant is suspended: " + tenantId);
    }
}
