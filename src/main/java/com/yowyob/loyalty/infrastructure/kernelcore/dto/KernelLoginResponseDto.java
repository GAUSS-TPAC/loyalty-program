package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Réponse de POST /api/auth/login sur KernelCore (auth-core).
 * Le nom exact du champ porteur du JWT n'est pas fixé par une spec partagée dans ce
 * dépôt (pas de collection Postman jointe) : on tolère plusieurs conventions usuelles.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelLoginResponseDto {

    private String accessToken;
    private String token;
    private String jwt;

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
}
