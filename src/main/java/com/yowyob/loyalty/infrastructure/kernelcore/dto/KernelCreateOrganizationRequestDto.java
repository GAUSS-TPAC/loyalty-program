package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import java.util.UUID;

/** Corps de requête pour POST /api/organizations sur KernelCore. */
public record KernelCreateOrganizationRequestDto(
        UUID businessActorId,
        String code,
        String legalName,
        String displayName,
        String organizationType
) {}
