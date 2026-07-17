package com.yowyob.loyalty.sdk;

import java.util.Map;

/** Erreur renvoyée par l'API Loyalty (statut HTTP non 2xx). */
public class ApiException extends LoyaltyException {

    private final int statusCode;
    private final transient Map<String, Object> body;

    public ApiException(String message, int statusCode, Map<String, Object> body) {
        super(message);
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, Object> getBody() {
        return body;
    }
}
