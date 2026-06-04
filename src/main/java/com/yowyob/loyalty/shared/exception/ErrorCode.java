package com.yowyob.loyalty.shared.exception;

public enum ErrorCode {
    TENANT_NOT_FOUND(404),
    TENANT_SUSPENDED(403),
    TENANT_NOT_READY(403),
    TENANT_CONTEXT_MISSING(500),
    JWT_INVALID(401),
    JWT_EXPIRED(401),
    JWT_MISSING(401),
    MISSING_TENANT_CLAIM(401),
    CROSS_TENANT_ACCESS_DENIED(403),
    FORBIDDEN(403),
    IDEMPOTENCY_CONFLICT(409),
    INTERNAL_ERROR(500),
    VALIDATION_ERROR(400),
    RESOURCE_NOT_FOUND(404),
    BONIFICATION_UNAVAILABLE(502);

    private final int httpStatus;

    ErrorCode(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
