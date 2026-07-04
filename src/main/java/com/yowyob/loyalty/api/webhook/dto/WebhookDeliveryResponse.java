package com.yowyob.loyalty.api.webhook.dto;

import com.yowyob.loyalty.domain.webhook.model.WebhookDelivery;

import java.time.Instant;
import java.util.UUID;

public record WebhookDeliveryResponse(
        UUID id,
        UUID endpointId,
        String eventType,
        String status,
        Integer httpStatusCode,
        String responseSnippet,
        int attemptCount,
        Instant createdAt,
        Instant deliveredAt
) {
    public static WebhookDeliveryResponse from(WebhookDelivery delivery) {
        return new WebhookDeliveryResponse(delivery.id(), delivery.endpointId(), delivery.eventType(),
                delivery.status().name(), delivery.httpStatusCode(), delivery.responseSnippet(),
                delivery.attemptCount(), delivery.createdAt(), delivery.deliveredAt());
    }
}
