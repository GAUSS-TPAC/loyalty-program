package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Réponse de POST /api/auth/discover-contexts sur KernelCore : liste des contextes
 * (tenants plateforme) accessibles au compte authentifié.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelDiscoverContextsResponseDto {

    private String selectionToken;
    private Long expiresInSeconds;
    private List<KernelDiscoveredContextDto> contexts;

    public String getSelectionToken() { return selectionToken; }
    public void setSelectionToken(String selectionToken) { this.selectionToken = selectionToken; }
    public Long getExpiresInSeconds() { return expiresInSeconds; }
    public void setExpiresInSeconds(Long expiresInSeconds) { this.expiresInSeconds = expiresInSeconds; }
    public List<KernelDiscoveredContextDto> getContexts() {
        return contexts != null ? contexts : List.of();
    }
    public void setContexts(List<KernelDiscoveredContextDto> contexts) { this.contexts = contexts; }
}
