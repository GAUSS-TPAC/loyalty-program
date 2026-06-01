package com.yowyob.loyalty.domain.shared.exception;

import java.util.Collections;
import java.util.Map;

public abstract class DomainException extends RuntimeException {
    private final Map<String, Object> details;

    protected DomainException(String message) {
        super(message);
        this.details = Collections.emptyMap();
    }

    protected DomainException(String message, Map<String, Object> details) {
        super(message);
        this.details = details != null ? Map.copyOf(details) : Collections.emptyMap();
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
