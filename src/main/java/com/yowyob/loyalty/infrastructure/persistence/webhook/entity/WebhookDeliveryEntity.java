package com.yowyob.loyalty.infrastructure.persistence.webhook.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("webhook_deliveries")
public class WebhookDeliveryEntity {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID endpointId;
    private String eventType;
    private String payload;
    private String status;
    private Integer httpStatusCode;
    private String responseSnippet;
    private int attemptCount;
    private Instant nextAttemptAt;
    private Instant createdAt;
    private Instant deliveredAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getEndpointId() { return endpointId; }
    public void setEndpointId(UUID endpointId) { this.endpointId = endpointId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getHttpStatusCode() { return httpStatusCode; }
    public void setHttpStatusCode(Integer httpStatusCode) { this.httpStatusCode = httpStatusCode; }
    public String getResponseSnippet() { return responseSnippet; }
    public void setResponseSnippet(String responseSnippet) { this.responseSnippet = responseSnippet; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public Instant getNextAttemptAt() { return nextAttemptAt; }
    public void setNextAttemptAt(Instant nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
}
