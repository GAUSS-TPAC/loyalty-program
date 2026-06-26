package com.yowyob.loyalty.shared.exception;

public class IdempotencyConflictException extends AppException {
    public IdempotencyConflictException(String idempotencyKey) {
        super(ErrorCode.IDEMPOTENCY_CONFLICT, "Request already processed for key: " + idempotencyKey);
    }
}
