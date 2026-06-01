package com.yowyob.loyalty.shared.exception;

public class JwtInvalidException extends AppException {
    public JwtInvalidException(String detail) {
        super(ErrorCode.JWT_INVALID, detail);
    }
}
