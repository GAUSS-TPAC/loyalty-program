package com.yowyob.loyaulty.program.shared.exception;

public class TenantNotFoundException extends AppException {
    public TenantNotFoundException(String tenantId) {
        super(ErrorCode.TENANT_NOT_FOUND, "Tenant not found: " + tenantId);
    }
}
