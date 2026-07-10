package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Réponse de POST /api/auth/login sur KernelCore (auth-core).
 * Le nom exact du champ porteur du JWT n'est pas fixé par une spec partagée dans ce
 * dépôt (pas de collection Postman jointe) : on tolère plusieurs conventions usuelles.
 *
 * Le JWT retourné (tenant-scoped) ne porte pas de claim d'organisation ({@code oid}) :
 * l'organisation cible doit être choisie parmi {@code organizations} (une entrée par
 * membership de l'acteur) et propagée par le client via le header {@code X-Organization-Id}
 * sur les appels suivants — voir KernelCoreAuthAdapter et le contrat documenté dans
 * docs/kernel_core documentation.md ("Contrat minimal backend vers kernel").
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelLoginResponseDto {

    private String accessToken;
    private String token;
    private String jwt;
    private List<KernelOrganizationSummaryDto> organizations;

    public String resolveAccessToken() {
        if (accessToken != null && !accessToken.isBlank()) return accessToken;
        if (token != null && !token.isBlank()) return token;
        return jwt;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getJwt() { return jwt; }
    public void setJwt(String jwt) { this.jwt = jwt; }
    public List<KernelOrganizationSummaryDto> getOrganizations() { return organizations; }
    public void setOrganizations(List<KernelOrganizationSummaryDto> organizations) { this.organizations = organizations; }
}
