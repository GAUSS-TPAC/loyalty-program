package com.yowyob.loyalty.domain.loyalty.exception;

import com.yowyob.loyalty.domain.shared.exception.DomainException;

import java.util.Map;

public class LoyaltyDomainException extends DomainException {

    private final Map<String, Object> details;

    public LoyaltyDomainException(String message) {
        super(message);
        this.details = null;
    }

    public LoyaltyDomainException(String message, Map<String, Object> details) {
        super(message);
        this.details = details;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
