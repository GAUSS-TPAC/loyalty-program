package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import java.util.List;

/**
 * Résultat exploitable d'un login KernelCore réussi : le JWT d'accès et la liste des
 * organisations accessibles à l'acteur authentifié (voir KernelLoginResponseDto).
 */
public record KernelLoginResultDto(String accessToken, List<KernelOrganizationSummaryDto> organizations) {
}
