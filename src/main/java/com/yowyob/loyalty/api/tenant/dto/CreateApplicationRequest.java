package com.yowyob.loyalty.api.tenant.dto;

import com.yowyob.loyalty.domain.tenant.model.enums.ApiKeyMode;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateApplicationRequest(
        @NotBlank String name,
        String description,
        String websiteUrl,
        String logoUrl,
        ApiKeyMode mode,
        String callbackUrl,
        List<String> eventTypes
) {}
