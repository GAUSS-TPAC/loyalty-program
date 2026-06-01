package com.yowyob.loyalty.shared.security;

import org.springframework.security.oauth2.jwt.Jwt;

public record JwtValidationResult(
    boolean isValid,
    Jwt jwt,
    String errorMessage
) {
    public static JwtValidationResult valid(Jwt jwt) {
        return new JwtValidationResult(true, jwt, null);
    }

    public static JwtValidationResult invalid(String errorMessage) {
        return new JwtValidationResult(false, null, errorMessage);
    }
}
