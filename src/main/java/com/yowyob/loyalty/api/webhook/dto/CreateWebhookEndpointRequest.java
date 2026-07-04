package com.yowyob.loyalty.api.webhook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateWebhookEndpointRequest(
        @NotBlank String url,
        String description,
        @NotEmpty List<String> eventTypes
) {}
