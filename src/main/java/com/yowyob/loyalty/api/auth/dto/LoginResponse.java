package com.yowyob.loyalty.api.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yowyob.loyalty.application.auth.AuthService;

/**
 * Réponse de login en deux formes possibles :
 * - authentifié : {@code token} + organisation active (organizationId à renvoyer par le client
 *   dans le header X-Organization-Id — le JWT KernelCore ne porte pas de claim d'organisation) ;
 * - défi MFA : {@code mfaRequired=true} + {@code mfaToken} — un code a été envoyé par email,
 *   à confirmer via POST /api/v1/auth/login/mfa.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponse(
        String token,
        String organizationId,
        String organizationCode,
        String organizationName,
        Boolean mfaRequired,
        String mfaToken,
        String mfaChannel
) {
    public LoginResponse(AuthService.AuthResult result) {
        this(result.token(), result.organizationId(), result.organizationCode(), result.organizationName(),
                null, null, null);
    }

    public static LoginResponse from(AuthService.LoginOutcome outcome) {
        if (outcome.isMfaRequired()) {
            return new LoginResponse(null, null, null, null, true, outcome.mfaToken(), outcome.mfaChannel());
        }
        return new LoginResponse(outcome.result());
    }
}
