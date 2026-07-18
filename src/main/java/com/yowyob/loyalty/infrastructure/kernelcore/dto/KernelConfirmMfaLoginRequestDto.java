package com.yowyob.loyalty.infrastructure.kernelcore.dto;

/** Corps de POST /api/auth/login/mfa/confirm (schéma ConfirmMfaLoginRequest de auth-core). */
public record KernelConfirmMfaLoginRequestDto(String mfaToken, String code) {}
