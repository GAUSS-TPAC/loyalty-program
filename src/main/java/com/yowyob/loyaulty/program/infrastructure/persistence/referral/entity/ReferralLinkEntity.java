package com.yowyob.loyaulty.program.infrastructure.persistence.referral.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("referral_links")
public class ReferralLinkEntity {
    @Id private UUID id;
    @Column("tenant_id")   private UUID tenantId;
    @Column("referrer_id") private String referrerId;
    @Column("code")        private String code;
    @Column("created_at")  private Instant createdAt;

    public UUID getId()               { return id; }
    public void setId(UUID id)        { this.id = id; }
    public UUID getTenantId()         { return tenantId; }
    public void setTenantId(UUID v)   { this.tenantId = v; }
    public String getReferrerId()     { return referrerId; }
    public void setReferrerId(String v){ this.referrerId = v; }
    public String getCode()           { return code; }
    public void setCode(String v)     { this.code = v; }
    public Instant getCreatedAt()     { return createdAt; }
    public void setCreatedAt(Instant v){ this.createdAt = v; }
}
