package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import java.util.List;

/**
 * Résultat exploitable d'un appel login KernelCore : soit un JWT d'accès et la liste des
 * organisations accessibles (login direct), soit un défi MFA à confirmer via
 * POST /api/auth/login/mfa/confirm (mfaToken + canal d'envoi du code) — voir
 * KernelLoginResponseDto.
 */
public record KernelLoginResultDto(
        String accessToken,
        List<KernelOrganizationSummaryDto> organizations,
        String mfaToken,
        String mfaChannel
) {

    public static KernelLoginResultDto authenticated(String accessToken, List<KernelOrganizationSummaryDto> organizations) {
        return new KernelLoginResultDto(accessToken, organizations, null, null);
    }

    public static KernelLoginResultDto mfaChallenge(String mfaToken, String mfaChannel) {
        return new KernelLoginResultDto(null, List.of(), mfaToken, mfaChannel);
    }

    public boolean mfaRequired() {
        return accessToken == null || accessToken.isBlank();
    }
}
