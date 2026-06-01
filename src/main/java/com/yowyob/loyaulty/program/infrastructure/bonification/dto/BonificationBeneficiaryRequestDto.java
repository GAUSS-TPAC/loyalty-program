package com.yowyob.loyaulty.program.infrastructure.bonification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BonificationBeneficiaryRequestDto(
        @JsonProperty("userId") String userId,
        @JsonProperty("email") String email,
        @JsonProperty("name") String name
) {}
