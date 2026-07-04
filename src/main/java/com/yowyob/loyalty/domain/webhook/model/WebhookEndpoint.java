package com.yowyob.loyalty.domain.webhook.model;

import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WebhookEndpoint(
        UUID id,
        TenantId tenantId,
        String url,
        String secret,
        String description,
        List<String> eventTypes,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static WebhookEndpoint create(TenantId tenantId, String url, String secret, String description, List<String> eventTypes) {
        Instant now = Instant.now();
        return new WebhookEndpoint(UUID.randomUUID(), tenantId, url, secret, description, eventTypes, true, now, now);
    }

    public boolean isSubscribedTo(String eventTypeCode) {
        return active && eventTypes.contains(eventTypeCode);
    }

    public WebhookEndpoint update(String url, String description, List<String> eventTypes) {
        return new WebhookEndpoint(id, tenantId, url, secret, description, eventTypes, active, createdAt, Instant.now());
    }

    public WebhookEndpoint rotateSecret(String newSecret) {
        return new WebhookEndpoint(id, tenantId, url, newSecret, description, eventTypes, active, createdAt, Instant.now());
    }

    public WebhookEndpoint activate() {
        return new WebhookEndpoint(id, tenantId, url, secret, description, eventTypes, true, createdAt, Instant.now());
    }

    public WebhookEndpoint deactivate() {
        return new WebhookEndpoint(id, tenantId, url, secret, description, eventTypes, false, createdAt, Instant.now());
    }
}
