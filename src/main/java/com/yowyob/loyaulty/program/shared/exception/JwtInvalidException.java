package com.yowyob.loyaulty.program.shared.exception;

public class JwtInvalidException extends AppException {
    public JwtInvalidException(String reason) {
        super(ErrorCode.JWT_INVALID, "Invalid JWT: " + reason);
    }
}
