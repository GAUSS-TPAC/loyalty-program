package com.yowyob.loyaulty.program.infrastructure.bonification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BonificationAuthResponseDto(
        @JsonProperty("token") String token,
        @JsonProperty("expiresIn") Long expiresIn
) {}
