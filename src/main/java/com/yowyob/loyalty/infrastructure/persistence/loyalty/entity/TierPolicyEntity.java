package com.yowyob.loyalty.infrastructure.persistence.loyalty.entity;

import io.r2dbc.postgresql.codec.Json;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("tier_policies")
public class TierPolicyEntity {

    @Id
    private UUID id;
    private UUID tenantId;
    private String criterion;
    private Json thresholds;
    private String maintainPeriod;
    private long maintainThresholdPoints;
    private int downgradeGraceDays;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getCriterion() { return criterion; }
    public void setCriterion(String criterion) { this.criterion = criterion; }
    public Json getThresholds() { return thresholds; }
    public void setThresholds(Json thresholds) { this.thresholds = thresholds; }
    public String getMaintainPeriod() { return maintainPeriod; }
    public void setMaintainPeriod(String maintainPeriod) { this.maintainPeriod = maintainPeriod; }
    public long getMaintainThresholdPoints() { return maintainThresholdPoints; }
    public void setMaintainThresholdPoints(long maintainThresholdPoints) { this.maintainThresholdPoints = maintainThresholdPoints; }
    public int getDowngradeGraceDays() { return downgradeGraceDays; }
    public void setDowngradeGraceDays(int downgradeGraceDays) { this.downgradeGraceDays = downgradeGraceDays; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
