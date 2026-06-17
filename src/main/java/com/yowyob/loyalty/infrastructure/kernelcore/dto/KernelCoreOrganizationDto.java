package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * DTO de réponse pour GET /api/organizations/{id} sur Kernel Core.
 * Les champs inconnus sont ignorés pour la compatibilité future.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KernelCoreOrganizationDto(
        @JsonProperty("id") UUID id,
        @JsonProperty("name") String name,
        @JsonProperty("slug") String slug,
        @JsonProperty("status") String status,
        @JsonProperty("plan") String plan
) {}
