package com.yowyob.loyalty.shared.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class TestJwtFactory {

    public static Jwt createValidJwt(UUID tenantId, String userId) {
        return Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("tenant_id", tenantId.toString())
                .claim("sub", userId)
                .claim("roles", List.of("ROLE_USER"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
