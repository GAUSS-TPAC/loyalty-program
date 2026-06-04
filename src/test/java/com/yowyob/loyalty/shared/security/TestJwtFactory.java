package com.yowyob.loyalty.shared.security;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Date;
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

    /** Unsigned JWT string parseable by {@link com.yowyob.loyalty.shared.security.JwtClaimsExtractor}. */
    public static String plainTokenForTenant(UUID tenantId, String userId) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .claim("tenant_id", tenantId.toString())
                    .subject(userId)
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                    .build();
            return new PlainJWT(claims).serialize();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build test JWT", e);
        }
    }

    public static String bearerHeaderForTenant(UUID tenantId, String userId) {
        return "Bearer " + plainTokenForTenant(tenantId, userId);
    }
}
