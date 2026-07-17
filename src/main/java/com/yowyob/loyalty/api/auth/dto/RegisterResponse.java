package com.yowyob.loyalty.api.auth.dto;

import com.yowyob.loyalty.application.auth.AuthService;

/**
 * @param status typiquement "EMAIL_VERIFICATION_REQUIRED" : le compte est créé mais le
 *               login échouera tant que l'email n'est pas confirmé.
 */
public record RegisterResponse(String email, String status, boolean emailVerified) {
    public RegisterResponse(AuthService.RegisterResult result) {
        this(result.email(), result.status(), result.emailVerified());
    }
}
