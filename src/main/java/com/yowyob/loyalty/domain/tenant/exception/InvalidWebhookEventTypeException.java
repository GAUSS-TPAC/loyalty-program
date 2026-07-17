package com.yowyob.loyalty.domain.tenant.exception;

public class InvalidWebhookEventTypeException extends ApplicationDomainException {
    public InvalidWebhookEventTypeException(String code) {
        super("Type d'événement webhook inconnu : " + code);
    }
}
