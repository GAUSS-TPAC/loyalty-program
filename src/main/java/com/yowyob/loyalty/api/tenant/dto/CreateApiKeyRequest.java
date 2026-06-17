package com.yowyob.loyalty.api.tenant.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateApiKeyRequest(@NotBlank String name) {}
