package com.yowyob.loyalty.shared.exception;

public class TenantSuspendedException extends AppException {
    public TenantSuspendedException(String detail) {
        super(ErrorCode.TENANT_SUSPENDED, detail);
    }
}
