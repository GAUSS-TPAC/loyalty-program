package com.yowyob.loyalty.infrastructure.persistence.reward.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("rewards")
public class RewardEntity {

    @Id
    private UUID id;
    @Column("tenant_id")
    private UUID tenantId;
    private String name;
    private String description;
    private String type;
    @Column("value_json")
    private String valueJson;
    @Column("cost_in_points")
    private long costInPoints;
    @Column("stock_total")
    private Integer stockTotal;
    @Column("stock_remaining")
    private Integer stockRemaining;
    @Column("valid_from")
    private Instant validFrom;
    @Column("valid_until")
    private Instant validUntil;
    @Column("grant_expiry_days")
    private int grantExpiryDays;
    @Column("image_url")
    private String imageUrl;
    @Column("metadata")
    private String metadataJson;
    private String status;
    private int version;
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
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getValueJson() { return valueJson; }
    public void setValueJson(String valueJson) { this.valueJson = valueJson; }
    public long getCostInPoints() { return costInPoints; }
    public void setCostInPoints(long costInPoints) { this.costInPoints = costInPoints; }
    public Integer getStockTotal() { return stockTotal; }
    public void setStockTotal(Integer stockTotal) { this.stockTotal = stockTotal; }
    public Integer getStockRemaining() { return stockRemaining; }
    public void setStockRemaining(Integer stockRemaining) { this.stockRemaining = stockRemaining; }
    public Instant getValidFrom() { return validFrom; }
    public void setValidFrom(Instant validFrom) { this.validFrom = validFrom; }
    public Instant getValidUntil() { return validUntil; }
    public void setValidUntil(Instant validUntil) { this.validUntil = validUntil; }
    public int getGrantExpiryDays() { return grantExpiryDays; }
    public void setGrantExpiryDays(int grantExpiryDays) { this.grantExpiryDays = grantExpiryDays; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
