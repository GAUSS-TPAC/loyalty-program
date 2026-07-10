package com.yowyob.loyalty.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * @param organizationId organisation KernelCore à sélectionner comme tenant actif ; optionnel
 *                        si l'acteur n'a accès qu'à une seule organisation.
 */
public record LoginRequest(@NotBlank String email, @NotBlank String password, String organizationId) {}
