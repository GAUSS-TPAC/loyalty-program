package com.yowyob.loyalty.infrastructure.kernelcore.dto;

/**
 * Corps de requête pour PUT /api/actors/me (Kernel Core BusinessActorRequest). "name" est requis côté Kernel Core.
 */
public record KernelBusinessActorUpdateRequest(
        String name,
        String contactPhone,
        String website,
        String biography,
        String businessId
) {}
