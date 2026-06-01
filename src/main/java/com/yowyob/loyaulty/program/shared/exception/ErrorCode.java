package com.yowyob.loyaulty.program.shared.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // Tenant
    TENANT_NOT_FOUND(HttpStatus.NOT_FOUND),
    TENANT_SUSPENDED(HttpStatus.FORBIDDEN),
    TENANT_NOT_READY(HttpStatus.FORBIDDEN),
    TENANT_CONTEXT_MISSING(HttpStatus.INTERNAL_SERVER_ERROR),

    // Auth / JWT
    JWT_MISSING(HttpStatus.UNAUTHORIZED),
    JWT_INVALID(HttpStatus.UNAUTHORIZED),
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED),
    MISSING_TENANT_CLAIM(HttpStatus.UNAUTHORIZED),
    CROSS_TENANT_ACCESS_DENIED(HttpStatus.FORBIDDEN),
    FORBIDDEN(HttpStatus.FORBIDDEN),

    // Wallet
    WALLET_NOT_FOUND(HttpStatus.NOT_FOUND),
    INSUFFICIENT_BALANCE(HttpStatus.UNPROCESSABLE_ENTITY),
    WALLET_FROZEN(HttpStatus.UNPROCESSABLE_ENTITY),
    DAILY_LIMIT_EXCEEDED(HttpStatus.UNPROCESSABLE_ENTITY),
    IDEMPOTENCY_CONFLICT(HttpStatus.CONFLICT),

    // Reward
    REWARD_NOT_FOUND(HttpStatus.NOT_FOUND),
    REWARD_OUT_OF_STOCK(HttpStatus.UNPROCESSABLE_ENTITY),
    REWARD_GRANT_NOT_FOUND(HttpStatus.NOT_FOUND),
    REWARD_ALREADY_USED(HttpStatus.UNPROCESSABLE_ENTITY),
    REWARD_EXPIRED(HttpStatus.UNPROCESSABLE_ENTITY),

    // Rule
    RULE_NOT_FOUND(HttpStatus.NOT_FOUND),
    RULE_VALIDATION_FAILED(HttpStatus.BAD_REQUEST),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND),

    // Bonification API externe
    BONIFICATION_API_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),
    BONIFICATION_API_AUTH_FAILED(HttpStatus.BAD_GATEWAY),
    BONIFICATION_API_TRANSACTION_FAILED(HttpStatus.BAD_GATEWAY),
    BONIFICATION_BENEFICIARY_NOT_FOUND(HttpStatus.NOT_FOUND),
    BONIFICATION_CIRCUIT_OPEN(HttpStatus.SERVICE_UNAVAILABLE),

    // Generic
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus httpStatus;

    ErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getStatusCode() {
        return httpStatus.value();
    }
}
