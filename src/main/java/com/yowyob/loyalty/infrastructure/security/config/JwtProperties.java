package com.yowyob.loyalty.infrastructure.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {
    private String issuerUri;
    private String jwkSetUri;
    private String audience;
    /**
     * Claim JWT KernelCore portant l'ID d'organisation ({@code oid}) — utilisé en repli
     * uniquement quand le client n'envoie pas le header X-Organization-Id (voir
     * TenantResolutionFilter). Un login KernelCore tenant-scopé "nu" ne porte pas ce
     * claim : {@code oid} n'apparaît que sur un JWT issu du flux discover/select-context.
     */
    private String tenantIdClaim = "oid";
    private String userIdClaim = "sub";
    /**
     * KernelCore auth-core émet une liste de codes de permission scopés (ex:
     * "tenant:admin", "tenant:admin#TENANT", "ROLE_OWNER") dans le claim
     * "permissions", pas de rôles Keycloak-style. Voir SecurityContextRepository
     * pour le mapping vers les autorités Spring ROLE_*.
     */
    private String rolesClaim = "permissions";
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
