package com.yowyob.loyalty.infrastructure.persistence.referral.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("referral_programs")
public class ReferralProgramEntity {

    @Id
    private UUID id;
    private UUID tenantId;
    private String name;
    private boolean active;
    private int maxReferralsPerReferrer;
    private int referralWindowDays;
    private UUID referrerRewardId;
    private UUID refereeRewardId;
    private int minConversionAmount;
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
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getMaxReferralsPerReferrer() { return maxReferralsPerReferrer; }
    public void setMaxReferralsPerReferrer(int maxReferralsPerReferrer) { this.maxReferralsPerReferrer = maxReferralsPerReferrer; }
    public int getReferralWindowDays() { return referralWindowDays; }
    public void setReferralWindowDays(int referralWindowDays) { this.referralWindowDays = referralWindowDays; }
    public UUID getReferrerRewardId() { return referrerRewardId; }
    public void setReferrerRewardId(UUID referrerRewardId) { this.referrerRewardId = referrerRewardId; }
    public UUID getRefereeRewardId() { return refereeRewardId; }
    public void setRefereeRewardId(UUID refereeRewardId) { this.refereeRewardId = refereeRewardId; }
    public int getMinConversionAmount() { return minConversionAmount; }
    public void setMinConversionAmount(int minConversionAmount) { this.minConversionAmount = minConversionAmount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
