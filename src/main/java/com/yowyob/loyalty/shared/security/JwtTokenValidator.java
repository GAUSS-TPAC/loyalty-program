package com.yowyob.loyalty.shared.security;

import com.yowyob.loyalty.infrastructure.security.config.JwtProperties;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

public class JwtTokenValidator {

    private final ReactiveJwtDecoder jwtDecoder;
    private final JwtProperties jwtProperties;

    public JwtTokenValidator(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        // In production, the JWK Set URI must be configured via properties
        String jwkSetUri = jwtProperties.getJwkSetUri() != null ? jwtProperties.getJwkSetUri() : "http://localhost/dummy";
        this.jwtDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    // Constructor for testing
    protected JwtTokenValidator(JwtProperties jwtProperties, ReactiveJwtDecoder jwtDecoder) {
        this.jwtProperties = jwtProperties;
        this.jwtDecoder = jwtDecoder;
    }

    public Mono<JwtValidationResult> validateToken(String token) {
        return jwtDecoder.decode(token)
                .map(jwt -> {
                    if (jwtProperties.getAudience() != null && !jwt.getAudience().contains(jwtProperties.getAudience())) {
                        return JwtValidationResult.invalid("Invalid audience");
                    }
                    return JwtValidationResult.valid(jwt);
                })
                .onErrorResume(e -> Mono.just(JwtValidationResult.invalid("Invalid signature or expired token: " + e.getMessage())));
    }
}
