package com.yowyob.loyaulty.program.infrastructure.persistence.referral.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Table("referral_programs")
public class ReferralProgramEntity {
    @Id private UUID id;
    @Column("tenant_id")    private UUID tenantId;
    @Column("active")       private boolean active;
    @Column("referrer_reward_type")  private String referrerRewardType;
    @Column("referrer_reward_value") private BigDecimal referrerRewardValue;
    @Column("referee_reward_type")   private String refereeRewardType;
    @Column("referee_reward_value")  private BigDecimal refereeRewardValue;
    @Column("conversion_event_type") private String conversionEventType;
    @Column("min_conversion_amount") private BigDecimal minConversionAmount;
    @Column("conversion_deadline_days") private int conversionDeadlineDays;

    public UUID getId()                   { return id; }
    public void setId(UUID id)            { this.id = id; }
    public UUID getTenantId()             { return tenantId; }
    public void setTenantId(UUID v)       { this.tenantId = v; }
    public boolean isActive()             { return active; }
    public void setActive(boolean v)      { this.active = v; }
    public String getReferrerRewardType()         { return referrerRewardType; }
    public void setReferrerRewardType(String v)   { this.referrerRewardType = v; }
    public BigDecimal getReferrerRewardValue()     { return referrerRewardValue; }
    public void setReferrerRewardValue(BigDecimal v) { this.referrerRewardValue = v; }
    public String getRefereeRewardType()          { return refereeRewardType; }
    public void setRefereeRewardType(String v)    { this.refereeRewardType = v; }
    public BigDecimal getRefereeRewardValue()      { return refereeRewardValue; }
    public void setRefereeRewardValue(BigDecimal v){ this.refereeRewardValue = v; }
    public String getConversionEventType()        { return conversionEventType; }
    public void setConversionEventType(String v)  { this.conversionEventType = v; }
    public BigDecimal getMinConversionAmount()     { return minConversionAmount; }
    public void setMinConversionAmount(BigDecimal v){ this.minConversionAmount = v; }
    public int getConversionDeadlineDays()        { return conversionDeadlineDays; }
    public void setConversionDeadlineDays(int v)  { this.conversionDeadlineDays = v; }
}
