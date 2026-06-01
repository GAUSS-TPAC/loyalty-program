package com.yowyob.loyalty.shared.exception;

public class CrossTenantAccessException extends AppException {
    public CrossTenantAccessException(String detail) {
        super(ErrorCode.CROSS_TENANT_ACCESS_DENIED, detail);
    }
}
