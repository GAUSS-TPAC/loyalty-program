package com.yowyob.loyalty.shared.exception;

public class TenantNotFoundException extends AppException {
    public TenantNotFoundException(String detail) {
        super(ErrorCode.TENANT_NOT_FOUND, detail);
    }
}
