package com.yowyob.loyaulty.program.shared.exception;

public class IdempotencyConflictException extends AppException {
    public IdempotencyConflictException(String key) {
        super(ErrorCode.IDEMPOTENCY_CONFLICT, "Request already processed: " + key);
    }
}
