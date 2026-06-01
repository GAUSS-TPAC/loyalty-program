package com.yowyob.loyaulty.program.shared.exception;

public class CrossTenantAccessException extends AppException {
    public CrossTenantAccessException() {
        super(ErrorCode.CROSS_TENANT_ACCESS_DENIED, "Cross-tenant access is not allowed");
    }
}
