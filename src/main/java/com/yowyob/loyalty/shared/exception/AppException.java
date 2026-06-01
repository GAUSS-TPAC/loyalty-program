package com.yowyob.loyalty.shared.exception;

import java.util.Collections;
import java.util.Map;

public abstract class AppException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, Object> properties;

    protected AppException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
        this.properties = Collections.emptyMap();
    }

    protected AppException(ErrorCode errorCode, String detail, Map<String, Object> properties) {
        super(detail);
        this.errorCode = errorCode;
        this.properties = properties != null ? Map.copyOf(properties) : Collections.emptyMap();
    }

    public int getHttpStatus() {
        return errorCode.getHttpStatus();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
