package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Contexte d'inscription (organisation) renvoyé par POST /api/auth/discover-sign-up-contexts.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelSignUpContextDto {

    private String contextId;
    private String tenantId;
    private String organizationId;
    private String organizationCode;
    private String organizationName;
    private String organizationType;

    public String getContextId() { return contextId; }
    public void setContextId(String contextId) { this.contextId = contextId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getOrganizationCode() { return organizationCode; }
    public void setOrganizationCode(String organizationCode) { this.organizationCode = organizationCode; }
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public String getOrganizationType() { return organizationType; }
    public void setOrganizationType(String organizationType) { this.organizationType = organizationType; }
}
