package com.yowyob.loyalty.infrastructure.persistence.referral.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("referral_links")
public class ReferralLinkEntity {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID referrerId;
    private String code;
    private Instant createdAt;
    private Instant expiresAt;
    private int usageCount;
    private int conversionCount;
    private boolean active;
    @Version
    private long version;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getReferrerId() { return referrerId; }
    public void setReferrerId(UUID referrerId) { this.referrerId = referrerId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public int getUsageCount() { return usageCount; }
    public void setUsageCount(int usageCount) { this.usageCount = usageCount; }
    public int getConversionCount() { return conversionCount; }
    public void setConversionCount(int conversionCount) { this.conversionCount = conversionCount; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
