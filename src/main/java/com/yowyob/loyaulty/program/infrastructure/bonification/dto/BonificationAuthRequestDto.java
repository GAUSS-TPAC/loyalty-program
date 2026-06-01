package com.yowyob.loyaulty.program.infrastructure.bonification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BonificationAuthRequestDto(
        @JsonProperty("username") String username,
        @JsonProperty("password") String password
) {}
