package com.yowyob.loyaulty.program.api.health.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TenantHealthResponse(
        String status,
        String tenantId,
        String tenantName,
        String tenantStatus,
        String tenantPlan,
        String userId,
        Set<String> roles,
        String apiVersion,
        Instant timestamp
) {
    public static TenantHealthResponse up(String tenantId, String tenantName,
                                           String tenantStatus, String tenantPlan,
                                           String userId, Set<String> roles) {
        return new TenantHealthResponse(
                "UP", tenantId, tenantName, tenantStatus, tenantPlan,
                userId, roles, "1.0.0", Instant.now()
        );
    }
}
