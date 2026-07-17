package com.yowyob.loyalty.domain.tenant.exception;

public class ApplicationDomainException extends RuntimeException {
    public ApplicationDomainException(String message) {
        super(message);
    }
}
