package com.yowyob.loyalty.infrastructure.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {
    private String issuerUri;
    private String jwkSetUri;
    private String audience;
    private String tenantIdClaim = "organization_id";
    private String userIdClaim = "sub";
    private String rolesClaim = "roles";
    private String scopesClaim = "scope";

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getJwkSetUri() {
        return jwkSetUri;
    }

    public void setJwkSetUri(String jwkSetUri) {
        this.jwkSetUri = jwkSetUri;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getTenantIdClaim() {
        return tenantIdClaim;
    }

    public void setTenantIdClaim(String tenantIdClaim) {
        this.tenantIdClaim = tenantIdClaim;
    }

    public String getUserIdClaim() {
        return userIdClaim;
    }

    public void setUserIdClaim(String userIdClaim) {
        this.userIdClaim = userIdClaim;
    }

    public String getRolesClaim() {
        return rolesClaim;
    }

    public void setRolesClaim(String rolesClaim) {
        this.rolesClaim = rolesClaim;
    }

    public String getScopesClaim() {
        return scopesClaim;
    }

    public void setScopesClaim(String scopesClaim) {
        this.scopesClaim = scopesClaim;
    }
}
