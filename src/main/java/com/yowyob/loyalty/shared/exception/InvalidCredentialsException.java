package com.yowyob.loyalty.shared.exception;

public class InvalidCredentialsException extends AppException {
    public InvalidCredentialsException(String detail) {
        super(ErrorCode.INVALID_CREDENTIALS, detail);
    }
}
