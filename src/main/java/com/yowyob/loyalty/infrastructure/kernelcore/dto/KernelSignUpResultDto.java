package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Réponse de POST /api/auth/sign-up : le compte est créé mais reste
 * EMAIL_VERIFICATION_REQUIRED tant que l'adresse n'est pas confirmée (le login échouera
 * jusque-là).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelSignUpResultDto {

    private boolean emailVerified;
    private String status;
    private String email;

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
