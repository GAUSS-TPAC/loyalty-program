package com.yowyob.loyalty.domain.shared.exception;

import java.util.Map;

public class DomainValidationException extends DomainException {
    public DomainValidationException(String message) {
        super(message);
    }

    public DomainValidationException(String message, Map<String, Object> details) {
        super(message, details);
    }
}
