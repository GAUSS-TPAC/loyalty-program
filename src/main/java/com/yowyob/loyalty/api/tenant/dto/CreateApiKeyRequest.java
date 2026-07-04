package com.yowyob.loyalty.api.tenant.dto;

import com.yowyob.loyalty.domain.tenant.model.enums.ApiKeyMode;
import jakarta.validation.constraints.NotBlank;

public record CreateApiKeyRequest(@NotBlank String name, ApiKeyMode mode) {}
