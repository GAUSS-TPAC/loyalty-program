package com.yowyob.loyalty.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Deuxième étape du login quand le compte a le MFA actif : le code reçu par email
 * et le mfaToken renvoyé par POST /api/v1/auth/login.
 *
 * @param organizationId organisation KernelCore à sélectionner comme tenant actif ; optionnel
 *                        si l'acteur n'a accès qu'à une seule organisation.
 */
public record ConfirmMfaRequest(@NotBlank String mfaToken, @NotBlank String code, String organizationId) {}
