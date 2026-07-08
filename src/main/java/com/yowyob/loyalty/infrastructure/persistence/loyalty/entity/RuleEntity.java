package com.yowyob.loyalty.infrastructure.persistence.loyalty.entity;

import io.r2dbc.postgresql.codec.Json;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("rules")
public class RuleEntity {

    @Id
    private UUID id;
    private UUID tenantId;
    private String name;
    private String description;
    private int priority;
    private String status;
    private Json triggerDefinition;
    private Json conditions;
    private Json effects;
    private Instant validFrom;
    private Instant validUntil;
    private int version;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Json getTriggerDefinition() { return triggerDefinition; }
    public void setTriggerDefinition(Json triggerDefinition) { this.triggerDefinition = triggerDefinition; }
    public Json getConditions() { return conditions; }
    public void setConditions(Json conditions) { this.conditions = conditions; }
    public Json getEffects() { return effects; }
    public void setEffects(Json effects) { this.effects = effects; }
    public Instant getValidFrom() { return validFrom; }
    public void setValidFrom(Instant validFrom) { this.validFrom = validFrom; }
    public Instant getValidUntil() { return validUntil; }
    public void setValidUntil(Instant validUntil) { this.validUntil = validUntil; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
