package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import java.util.List;

/** Corps de requête pour POST /api/roles (Kernel Core CreateRoleRequest). */
public record KernelCreateRoleRequest(String code, String name, String scopeType, List<String> permissions) {}
