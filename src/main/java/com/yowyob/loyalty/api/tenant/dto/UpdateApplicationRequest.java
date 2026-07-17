package com.yowyob.loyalty.api.tenant.dto;

import java.util.List;

public record UpdateApplicationRequest(
        String name,
        String description,
        String websiteUrl,
        String logoUrl,
        String callbackUrl,
        List<String> eventTypes
) {}
