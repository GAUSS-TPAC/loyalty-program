package com.yowyob.loyalty.api.bonification.dto;

public record BonificationStatusResponse(
        boolean enabled,
        boolean reachable,
        String baseUrl,
        String message
) {}
