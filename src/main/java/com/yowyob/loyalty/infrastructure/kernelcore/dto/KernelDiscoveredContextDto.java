package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Contexte de connexion découvert via POST /api/auth/discover-contexts sur KernelCore :
 * un tenant plateforme auquel le compte (email/mot de passe) a accès, avec les
 * organisations accessibles sous ce tenant.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelDiscoveredContextDto {

    private String contextId;
    private String tenantId;
    private String userId;
    private String actorId;
    private List<KernelOrganizationSummaryDto> organizations;

    public String getContextId() { return contextId; }
    public void setContextId(String contextId) { this.contextId = contextId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }
    public List<KernelOrganizationSummaryDto> getOrganizations() {
        return organizations != null ? organizations : List.of();
    }
    public void setOrganizations(List<KernelOrganizationSummaryDto> organizations) { this.organizations = organizations; }
}
