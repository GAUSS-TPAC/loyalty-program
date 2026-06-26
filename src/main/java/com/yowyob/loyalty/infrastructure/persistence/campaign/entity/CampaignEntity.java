package com.yowyob.loyalty.infrastructure.persistence.campaign.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("campaigns")
public class CampaignEntity {

    @Id
    private UUID id;
    private UUID tenantId;
    private String name;
    private String description;
    private String campaignType;
    private String targetEventType;
    private BigDecimal bonusMultiplier;
    private long bonusPoints;
    private Instant startDate;
    private Instant endDate;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    @Version
    private long version;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCampaignType() { return campaignType; }
    public void setCampaignType(String campaignType) { this.campaignType = campaignType; }
    public String getTargetEventType() { return targetEventType; }
    public void setTargetEventType(String targetEventType) { this.targetEventType = targetEventType; }
    public BigDecimal getBonusMultiplier() { return bonusMultiplier; }
    public void setBonusMultiplier(BigDecimal bonusMultiplier) { this.bonusMultiplier = bonusMultiplier; }
    public long getBonusPoints() { return bonusPoints; }
    public void setBonusPoints(long bonusPoints) { this.bonusPoints = bonusPoints; }
    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }
    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
