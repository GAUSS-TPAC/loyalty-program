package com.yowyob.loyalty.domain.tenant.exception;

import java.util.UUID;

public class ApplicationNotFoundException extends ApplicationDomainException {
    public ApplicationNotFoundException(UUID id) {
        super("Integration application not found: " + id);
    }

    public ApplicationNotFoundException(String publicKey) {
        super("Integration application not found for public key: " + publicKey);
    }
}
