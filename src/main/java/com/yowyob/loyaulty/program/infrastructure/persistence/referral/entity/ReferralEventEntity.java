package com.yowyob.loyaulty.program.infrastructure.persistence.referral.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("referral_events")
public class ReferralEventEntity {
    @Id private UUID id;
    @Column("tenant_id")    private UUID    tenantId;
    @Column("referrer_id")  private String  referrerId;
    @Column("referee_id")   private String  refereeId;
    @Column("referral_code")private String  referralCode;
    @Column("status")       private String  status;
    @Column("enrolled_at")  private Instant enrolledAt;
    @Column("converted_at") private Instant convertedAt;
    @Column("rewarded_at")  private Instant rewardedAt;
    @Column("expires_at")   private Instant expiresAt;

    public UUID getId()                  { return id; }
    public void setId(UUID v)            { this.id = v; }
    public UUID getTenantId()            { return tenantId; }
    public void setTenantId(UUID v)      { this.tenantId = v; }
    public String getReferrerId()        { return referrerId; }
    public void setReferrerId(String v)  { this.referrerId = v; }
    public String getRefereeId()         { return refereeId; }
    public void setRefereeId(String v)   { this.refereeId = v; }
    public String getReferralCode()      { return referralCode; }
    public void setReferralCode(String v){ this.referralCode = v; }
    public String getStatus()            { return status; }
    public void setStatus(String v)      { this.status = v; }
    public Instant getEnrolledAt()       { return enrolledAt; }
    public void setEnrolledAt(Instant v) { this.enrolledAt = v; }
    public Instant getConvertedAt()      { return convertedAt; }
    public void setConvertedAt(Instant v){ this.convertedAt = v; }
    public Instant getRewardedAt()       { return rewardedAt; }
    public void setRewardedAt(Instant v) { this.rewardedAt = v; }
    public Instant getExpiresAt()        { return expiresAt; }
    public void setExpiresAt(Instant v)  { this.expiresAt = v; }
}
