package com.yowyob.loyaulty.program.infrastructure.bonification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record BonificationTransactionRequestDto(
        @JsonProperty("userId") String userId,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("description") String description
) {}
