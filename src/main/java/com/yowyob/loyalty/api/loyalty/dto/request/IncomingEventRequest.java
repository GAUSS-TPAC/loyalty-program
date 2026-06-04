package com.yowyob.loyalty.api.loyalty.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

public record IncomingEventRequest(
        @NotBlank String eventType,
        @NotBlank String memberId,
        @NotNull Instant occurredAt,
        Map<String, Object> payload
) {}
