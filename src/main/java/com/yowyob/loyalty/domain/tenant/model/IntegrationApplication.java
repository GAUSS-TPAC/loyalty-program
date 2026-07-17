package com.yowyob.loyalty.domain.tenant.model;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.enums.ApiKeyMode;

import java.time.Instant;
import java.util.UUID;

/**
 * Application d'intégration : regroupe pour un projet tiers d'un tenant
 * une clé publique (identifiant exposable), une clé API privée (authentification)
 * et un éventuel endpoint webhook (callbacks signés).
 */
public record IntegrationApplication(
        UUID id,
        TenantId tenantId,
        String name,
        String description,
        String websiteUrl,
        String logoUrl,
        String publicKey,
        UUID apiKeyId,
        UUID webhookEndpointId,
        ApiKeyMode mode,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static IntegrationApplication create(TenantId tenantId, String name, String description,
                                                String websiteUrl, String logoUrl, String publicKey,
                                                UUID apiKeyId, UUID webhookEndpointId, ApiKeyMode mode) {
        Instant now = Instant.now();
        return new IntegrationApplication(UUID.randomUUID(), tenantId, name, description, websiteUrl, logoUrl,
                publicKey, apiKeyId, webhookEndpointId, mode, true, now, now);
    }

    public IntegrationApplication update(String name, String description, String websiteUrl, String logoUrl) {
        return new IntegrationApplication(id, tenantId, name, description, websiteUrl, logoUrl,
                publicKey, apiKeyId, webhookEndpointId, mode, active, createdAt, Instant.now());
    }

    public IntegrationApplication withApiKey(UUID newApiKeyId) {
        return new IntegrationApplication(id, tenantId, name, description, websiteUrl, logoUrl,
                publicKey, newApiKeyId, webhookEndpointId, mode, active, createdAt, Instant.now());
    }

    public IntegrationApplication withWebhookEndpoint(UUID newWebhookEndpointId) {
        return new IntegrationApplication(id, tenantId, name, description, websiteUrl, logoUrl,
                publicKey, apiKeyId, newWebhookEndpointId, mode, active, createdAt, Instant.now());
    }

    public IntegrationApplication withoutWebhookEndpoint() {
        return withWebhookEndpoint(null);
    }

    public IntegrationApplication deactivate() {
        return new IntegrationApplication(id, tenantId, name, description, websiteUrl, logoUrl,
                publicKey, apiKeyId, webhookEndpointId, mode, false, createdAt, Instant.now());
    }
}
