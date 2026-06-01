package com.yowyob.loyalty.shared.exception;

public class JwtExpiredException extends AppException {
    public JwtExpiredException(String detail) {
        super(ErrorCode.JWT_EXPIRED, detail);
    }
}
