package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Résumé d'organisation tel qu'inclus dans le champ {@code organizations} de la
 * réponse de POST /api/auth/login sur KernelCore (une organisation par membership
 * de l'acteur authentifié).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelOrganizationSummaryDto {

    private String organizationId;
    private String organizationCode;
    private String displayName;
    private List<String> services;

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getOrganizationCode() { return organizationCode; }
    public void setOrganizationCode(String organizationCode) { this.organizationCode = organizationCode; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public List<String> getServices() { return services; }
    public void setServices(List<String> services) { this.services = services; }
}
