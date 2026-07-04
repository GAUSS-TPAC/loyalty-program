package com.yowyob.loyalty.domain.webhook.model;

import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record WebhookDelivery(
        UUID id,
        TenantId tenantId,
        UUID endpointId,
        String eventType,
        String payload,
        DeliveryStatus status,
        Integer httpStatusCode,
        String responseSnippet,
        int attemptCount,
        Instant nextAttemptAt,
        Instant createdAt,
        Instant deliveredAt
) {
    private static final int MAX_ATTEMPTS = 6;
    private static final long BASE_BACKOFF_SECONDS = 30;

    public static WebhookDelivery createPending(TenantId tenantId, UUID endpointId, String eventType, String payload) {
        return new WebhookDelivery(UUID.randomUUID(), tenantId, endpointId, eventType, payload,
                DeliveryStatus.PENDING, null, null, 0, null, Instant.now(), null);
    }

    public WebhookDelivery markSucceeded(int httpStatusCode, String responseSnippet) {
        return new WebhookDelivery(id, tenantId, endpointId, eventType, payload, DeliveryStatus.SUCCEEDED,
                httpStatusCode, responseSnippet, attemptCount + 1, null, createdAt, Instant.now());
    }

    public WebhookDelivery markFailed(Integer httpStatusCode, String responseSnippet) {
        int attempts = attemptCount + 1;
        boolean exhausted = attempts >= MAX_ATTEMPTS;
        Instant next = exhausted ? null : Instant.now().plusSeconds(nextBackoffSeconds(attempts));
        return new WebhookDelivery(id, tenantId, endpointId, eventType, payload,
                exhausted ? DeliveryStatus.EXHAUSTED : DeliveryStatus.FAILED,
                httpStatusCode, responseSnippet, attempts, next, createdAt, null);
    }

    private static long nextBackoffSeconds(int attempt) {
        return BASE_BACKOFF_SECONDS * (1L << Math.min(attempt, 10));
    }

    public Duration timeSinceCreation() {
        return Duration.between(createdAt, Instant.now());
    }
}
