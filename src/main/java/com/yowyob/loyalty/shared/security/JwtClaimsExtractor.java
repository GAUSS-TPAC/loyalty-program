package com.yowyob.loyalty.shared.security;

import com.nimbusds.jwt.JWTParser;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.security.config.JwtProperties;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JwtClaimsExtractor {

    private final JwtProperties jwtProperties;

    public JwtClaimsExtractor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * Extracts the tenant ID from an already-verified Spring Security Jwt.
     * Use this in production filters — the JWT signature has already been checked.
     */
    public TenantId extractTenantId(Jwt jwt) {
        Object claim = jwt.getClaim(jwtProperties.getTenantIdClaim());
        if (claim == null) {
            throw new IllegalArgumentException("Tenant claim '" + jwtProperties.getTenantIdClaim() + "' missing in token");
        }
        return TenantId.of(UUID.fromString(claim.toString()));
    }

    /**
     * Dev/fallback only — parses a raw JWT without verifying the signature.
     * MUST NOT be used on authenticated production paths.
     */
    public TenantId extractTenantIdFromRawToken(String token) throws Exception {
        Object claim = JWTParser.parse(token).getJWTClaimsSet().getClaim(jwtProperties.getTenantIdClaim());
        if (claim == null) {
            throw new IllegalArgumentException("Tenant claim missing in token");
        }
        return TenantId.of(UUID.fromString(claim.toString()));
    }
}
