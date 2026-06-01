package com.yowyob.loyaulty.program.infrastructure.bonification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BonificationHistoryItemDto(
        @JsonProperty("id") String id,
        @JsonProperty("userId") String userId,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("description") String description,
        @JsonProperty("pointsEarned") Integer pointsEarned,
        @JsonProperty("status") String status,
        @JsonProperty("createdAt") String createdAt
) {}
