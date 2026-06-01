package com.yowyob.loyaulty.program.infrastructure.persistence.rule.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("rules")
public class RuleEntity {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    private String name;
    private String description;
    private int priority;
    private String status;

    @Column("trigger_def")
    private String triggerDef;      // JSON

    @Column("conditions_def")
    private String conditionsDef;   // JSON array

    @Column("effects_def")
    private String effectsDef;      // JSON array

    @Column("valid_from")
    private Instant validFrom;

    @Column("valid_until")
    private Instant validUntil;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    @Column("created_by")
    private String createdBy;

    // ── Accesseurs ────────────────────────────────────────────────────────

    public UUID getId()                { return id; }
    public void setId(UUID id)         { this.id = id; }

    public UUID getTenantId()              { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getName()               { return name; }
    public void setName(String name)      { this.name = name; }

    public String getDescription()                  { return description; }
    public void setDescription(String description)  { this.description = description; }

    public int getPriority()                { return priority; }
    public void setPriority(int priority)   { this.priority = priority; }

    public String getStatus()              { return status; }
    public void setStatus(String status)   { this.status = status; }

    public String getTriggerDef()                   { return triggerDef; }
    public void setTriggerDef(String triggerDef)    { this.triggerDef = triggerDef; }

    public String getConditionsDef()                    { return conditionsDef; }
    public void setConditionsDef(String conditionsDef)  { this.conditionsDef = conditionsDef; }

    public String getEffectsDef()                   { return effectsDef; }
    public void setEffectsDef(String effectsDef)    { this.effectsDef = effectsDef; }

    public Instant getValidFrom()                { return validFrom; }
    public void setValidFrom(Instant validFrom)  { this.validFrom = validFrom; }

    public Instant getValidUntil()                 { return validUntil; }
    public void setValidUntil(Instant validUntil)  { this.validUntil = validUntil; }

    public Instant getCreatedAt()                { return createdAt; }
    public void setCreatedAt(Instant createdAt)  { this.createdAt = createdAt; }

    public Instant getUpdatedAt()                { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt)  { this.updatedAt = updatedAt; }

    public String getCreatedBy()                 { return createdBy; }
    public void setCreatedBy(String createdBy)   { this.createdBy = createdBy; }
}
