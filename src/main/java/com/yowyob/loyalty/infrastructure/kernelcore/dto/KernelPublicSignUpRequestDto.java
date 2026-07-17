package com.yowyob.loyalty.infrastructure.kernelcore.dto;

/**
 * Corps de requête pour POST /api/auth/sign-up (inscription publique KernelCore).
 * Le selectionToken vient de POST /api/auth/discover-sign-up-contexts.
 */
public record KernelPublicSignUpRequestDto(
        String selectionToken,
        String firstName,
        String lastName,
        String email,
        String password
) {}
