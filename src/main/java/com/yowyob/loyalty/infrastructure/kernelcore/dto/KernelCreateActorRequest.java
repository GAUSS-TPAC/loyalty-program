package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import java.util.UUID;

/** Corps de requête pour POST /api/actors (Kernel Core CreateActorRequest). */
public record KernelCreateActorRequest(UUID organizationId, String firstName, String lastName, String email) {}
