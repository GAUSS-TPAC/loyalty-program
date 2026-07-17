package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import java.util.UUID;

/**
 * Corps de requête pour POST /api/auth/register (Kernel Core RegisterUserRequest).
 * Pas de "password" : le compte est créé sans mot de passe, puis
 * KernelCoreDeveloperInviteAdapter#sendPasswordSetupEmail déclenche l'email Kernel Core
 * de définition de mot de passe (POST /api/auth/forgot-password).
 */
public record KernelRegisterUserRequest(UUID actorId, String username, String email, String authProvider) {}
