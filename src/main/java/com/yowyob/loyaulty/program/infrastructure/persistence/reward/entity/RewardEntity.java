package com.yowyob.loyaulty.program.infrastructure.persistence.reward.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("rewards")
public class RewardEntity {
    @Id private UUID id;
    @Column("tenant_id")   private UUID tenantId;
    @Column("name")        private String name;
    @Column("description") private String description;
    @Column("type")        private String type;
    @Column("cost_points") private long costPoints;
    @Column("stock")       private Integer stock;
    @Column("status")      private String status;
    @Column("valid_from")  private Instant validFrom;
    @Column("valid_until") private Instant validUntil;
    @Column("created_at")  private Instant createdAt;
    @Column("updated_at")  private Instant updatedAt;

    public UUID getId()                { return id; }
    public void setId(UUID v)          { this.id = v; }
    public UUID getTenantId()          { return tenantId; }
    public void setTenantId(UUID v)    { this.tenantId = v; }
    public String getName()            { return name; }
    public void setName(String v)      { this.name = v; }
    public String getDescription()     { return description; }
    public void setDescription(String v){ this.description = v; }
    public String getType()            { return type; }
    public void setType(String v)      { this.type = v; }
    public long getCostPoints()        { return costPoints; }
    public void setCostPoints(long v)  { this.costPoints = v; }
    public Integer getStock()          { return stock; }
    public void setStock(Integer v)    { this.stock = v; }
    public String getStatus()          { return status; }
    public void setStatus(String v)    { this.status = v; }
    public Instant getValidFrom()      { return validFrom; }
    public void setValidFrom(Instant v){ this.validFrom = v; }
    public Instant getValidUntil()     { return validUntil; }
    public void setValidUntil(Instant v){ this.validUntil = v; }
    public Instant getCreatedAt()      { return createdAt; }
    public void setCreatedAt(Instant v){ this.createdAt = v; }
    public Instant getUpdatedAt()      { return updatedAt; }
    public void setUpdatedAt(Instant v){ this.updatedAt = v; }
}
