package com.yowyob.loyalty.sdk;

import java.util.Map;

/** Clé API invalide, révoquée ou absente (HTTP 401/403). */
public class AuthenticationException extends ApiException {
    public AuthenticationException(String message, int statusCode, Map<String, Object> body) {
        super(message, statusCode, body);
    }
}
