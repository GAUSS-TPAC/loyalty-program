package com.yowyob.loyaulty.program.shared.exception;

import org.springframework.http.HttpStatus;

public abstract class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detail;

    protected AppException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
        this.detail = detail;
    }

    protected AppException(ErrorCode errorCode, String detail, Throwable cause) {
        super(detail, cause);
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDetail() {
        return detail;
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}
