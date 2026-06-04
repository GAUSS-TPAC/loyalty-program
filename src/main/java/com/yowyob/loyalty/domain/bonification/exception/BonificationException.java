package com.yowyob.loyalty.domain.bonification.exception;

import com.yowyob.loyalty.domain.shared.exception.DomainException;

public class BonificationException extends DomainException {

    public BonificationException(String message) {
        super(message);
    }

    public BonificationException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
