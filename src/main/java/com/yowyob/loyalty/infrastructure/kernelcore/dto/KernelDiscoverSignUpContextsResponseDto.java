package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Réponse de POST /api/auth/discover-sign-up-contexts sur KernelCore : jeton court
 * réutilisable par POST /api/auth/sign-up, et le ou les contextes (organisation)
 * disponibles pour ce code d'organisation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelDiscoverSignUpContextsResponseDto {

    private String selectionToken;
    private Long expiresInSeconds;
    private List<KernelSignUpContextDto> contexts;

    public String getSelectionToken() { return selectionToken; }
    public void setSelectionToken(String selectionToken) { this.selectionToken = selectionToken; }
    public Long getExpiresInSeconds() { return expiresInSeconds; }
    public void setExpiresInSeconds(Long expiresInSeconds) { this.expiresInSeconds = expiresInSeconds; }
    public List<KernelSignUpContextDto> getContexts() {
        return contexts != null ? contexts : List.of();
    }
    public void setContexts(List<KernelSignUpContextDto> contexts) { this.contexts = contexts; }
}
