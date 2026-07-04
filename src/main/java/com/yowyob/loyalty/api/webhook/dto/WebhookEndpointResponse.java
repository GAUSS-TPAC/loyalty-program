package com.yowyob.loyalty.api.webhook.dto;

import com.yowyob.loyalty.domain.webhook.model.WebhookEndpoint;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WebhookEndpointResponse(
        UUID id,
        String url,
        String description,
        List<String> eventTypes,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        String secret
) {
    public static WebhookEndpointResponse from(WebhookEndpoint endpoint) {
        return new WebhookEndpointResponse(endpoint.id(), endpoint.url(), endpoint.description(),
                endpoint.eventTypes(), endpoint.active(), endpoint.createdAt(), endpoint.updatedAt(), null);
    }

    public static WebhookEndpointResponse withSecret(WebhookEndpoint endpoint, String secret) {
        return new WebhookEndpointResponse(endpoint.id(), endpoint.url(), endpoint.description(),
                endpoint.eventTypes(), endpoint.active(), endpoint.createdAt(), endpoint.updatedAt(), secret);
    }
}
