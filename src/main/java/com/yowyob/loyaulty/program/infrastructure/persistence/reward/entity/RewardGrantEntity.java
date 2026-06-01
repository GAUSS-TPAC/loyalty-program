package com.yowyob.loyaulty.program.infrastructure.persistence.reward.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("reward_grants")
public class RewardGrantEntity {
    @Id private UUID id;
    @Column("tenant_id")   private UUID tenantId;
    @Column("member_id")   private String memberId;
    @Column("reward_id")   private UUID rewardId;
    @Column("status")      private String status;
    @Column("granted_at")  private Instant grantedAt;
    @Column("expires_at")  private Instant expiresAt;
    @Column("used_at")     private Instant usedAt;
    @Column("use_context") private String useContext;

    public UUID getId()                  { return id; }
    public void setId(UUID v)            { this.id = v; }
    public UUID getTenantId()            { return tenantId; }
    public void setTenantId(UUID v)      { this.tenantId = v; }
    public String getMemberId()          { return memberId; }
    public void setMemberId(String v)    { this.memberId = v; }
    public UUID getRewardId()            { return rewardId; }
    public void setRewardId(UUID v)      { this.rewardId = v; }
    public String getStatus()            { return status; }
    public void setStatus(String v)      { this.status = v; }
    public Instant getGrantedAt()        { return grantedAt; }
    public void setGrantedAt(Instant v)  { this.grantedAt = v; }
    public Instant getExpiresAt()        { return expiresAt; }
    public void setExpiresAt(Instant v)  { this.expiresAt = v; }
    public Instant getUsedAt()           { return usedAt; }
    public void setUsedAt(Instant v)     { this.usedAt = v; }
    public String getUseContext()        { return useContext; }
    public void setUseContext(String v)  { this.useContext = v; }
}
