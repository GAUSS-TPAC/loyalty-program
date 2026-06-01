package com.yowyob.loyalty.api.tenant.dto;

import java.util.UUID;

public record TenantHealthResponse(UUID tenantId, String tenantName, String status) {}
