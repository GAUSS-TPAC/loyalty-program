package com.yowyob.loyaulty.program.api.rule.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Map;

public record SendEventRequest(
        @NotBlank(message = "eventType is required")
        String eventType,

        @NotBlank(message = "memberId is required")
        String memberId,

        Instant occurredAt,

        Map<String, Object> payload
) {}
