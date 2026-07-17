package com.yowyob.loyalty.infrastructure.persistence.tenant.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("integration_applications")
public class IntegrationApplicationEntity {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    private String name;

    private String description;

    @Column("website_url")
    private String websiteUrl;

    @Column("logo_url")
    private String logoUrl;

    @Column("public_key")
    private String publicKey;

    @Column("api_key_id")
    private UUID apiKeyId;

    @Column("webhook_endpoint_id")
    private UUID webhookEndpointId;

    private String mode;

    private boolean active;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    public UUID getApiKeyId() { return apiKeyId; }
    public void setApiKeyId(UUID apiKeyId) { this.apiKeyId = apiKeyId; }
    public UUID getWebhookEndpointId() { return webhookEndpointId; }
    public void setWebhookEndpointId(UUID webhookEndpointId) { this.webhookEndpointId = webhookEndpointId; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
