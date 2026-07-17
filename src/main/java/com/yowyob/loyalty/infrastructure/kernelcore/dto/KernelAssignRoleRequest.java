package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import java.util.UUID;

/** Corps de requête pour POST /api/roles/assignments (Kernel Core AssignRoleToUserRequest). */
public record KernelAssignRoleRequest(UUID userId, UUID roleId, String scopeType, UUID scopeId, String scope) {}
