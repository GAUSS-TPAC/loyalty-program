package com.yowyob.loyaulty.program.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String issuerUri,
        String jwkSetUri,
        String audience,
        String tenantIdClaim,
        String userIdClaim,
        String rolesClaim
) {
    public JwtProperties {
        if (tenantIdClaim == null) tenantIdClaim = "tenant_id";
        if (userIdClaim == null) userIdClaim = "sub";
        if (rolesClaim == null) rolesClaim = "roles";
    }
}
