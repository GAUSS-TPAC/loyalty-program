package com.yowyob.loyalty.shared.security;

import com.nimbusds.jwt.JWTParser;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.security.config.JwtProperties;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JwtClaimsExtractor {

    private final JwtProperties jwtProperties;

    public JwtClaimsExtractor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public TenantId extractTenantIdFromRawToken(String token) throws Exception {
        Object claim = JWTParser.parse(token).getJWTClaimsSet().getClaim(jwtProperties.getTenantIdClaim());
        if (claim == null) {
            throw new IllegalArgumentException("Tenant claim missing in token");
        }
        return TenantId.of(UUID.fromString(claim.toString()));
    }
}
