package com.yowyob.loyaulty.program.shared.exception;

public class BonificationApiException extends AppException {

    public BonificationApiException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public BonificationApiException(ErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode, detail, cause);
    }
}
