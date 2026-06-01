package com.yowyob.loyalty.shared.exception;

public class ForbiddenException extends AppException {
    public ForbiddenException(String detail) {
        super(ErrorCode.FORBIDDEN, detail);
    }
}
