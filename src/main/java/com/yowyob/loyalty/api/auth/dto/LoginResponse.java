package com.yowyob.loyalty.api.auth.dto;

import com.yowyob.loyalty.application.auth.AuthService;

/**
 * @param organizationId à renvoyer par le client dans le header X-Organization-Id sur les
 *                        appels suivants (le JWT KernelCore ne porte pas de claim d'organisation).
 */
public record LoginResponse(String token, String organizationId, String organizationCode, String organizationName) {
    public LoginResponse(AuthService.AuthResult result) {
        this(result.token(), result.organizationId(), result.organizationCode(), result.organizationName());
    }
}
