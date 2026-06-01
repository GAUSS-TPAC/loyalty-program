package com.yowyob.loyaulty.program.shared.exception;

public class ForbiddenException extends AppException {

    public ForbiddenException(String detail) {
        super(ErrorCode.FORBIDDEN, detail);
    }

    public ForbiddenException() {
        super(ErrorCode.FORBIDDEN, "Access to this resource is not allowed");
    }
}
