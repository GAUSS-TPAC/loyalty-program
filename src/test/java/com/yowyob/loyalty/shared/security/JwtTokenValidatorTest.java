package com.yowyob.loyalty.shared.security;

import com.yowyob.loyalty.infrastructure.security.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class JwtTokenValidatorTest {

    private JwtProperties jwtProperties;
    private ReactiveJwtDecoder jwtDecoder;
    private JwtTokenValidator validator;

    @BeforeEach
    public void setup() {
        jwtProperties = new JwtProperties();
        jwtProperties.setAudience("loyalty-service");
        jwtDecoder = Mockito.mock(ReactiveJwtDecoder.class);
        validator = new JwtTokenValidator(jwtProperties, jwtDecoder);
    }

    @Test
    public void testValidToken() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "user1")
                .audience(List.of("loyalty-service"))
                .build();
        
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        StepVerifier.create(validator.validateToken("token"))
                .expectNextMatches(JwtValidationResult::isValid)
                .verifyComplete();
    }

    @Test
    public void testInvalidAudience() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "user1")
                .audience(List.of("other-service"))
                .build();
        
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        StepVerifier.create(validator.validateToken("token"))
                .expectNextMatches(result -> !result.isValid() && "Invalid audience".equals(result.errorMessage()))
                .verifyComplete();
    }

    @Test
    public void testInvalidSignature() {
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.error(new JwtException("Invalid signature")));

        StepVerifier.create(validator.validateToken("token"))
                .expectNextMatches(result -> !result.isValid() && result.errorMessage().contains("Invalid signature"))
                .verifyComplete();
    }
}
