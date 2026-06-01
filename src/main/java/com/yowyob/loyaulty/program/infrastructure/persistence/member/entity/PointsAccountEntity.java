package com.yowyob.loyaulty.program.infrastructure.persistence.member.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("points_accounts")
public class PointsAccountEntity {
    @Id private UUID id;
    @Column("tenant_id")        private UUID tenantId;
    @Column("member_id")        private String memberId;
    @Column("available_points") private long availablePoints;
    @Column("lifetime_earned")  private long lifetimeEarned;
    @Column("lifetime_spent")   private long lifetimeSpent;

    public UUID getId()                    { return id; }
    public void setId(UUID v)              { this.id = v; }
    public UUID getTenantId()              { return tenantId; }
    public void setTenantId(UUID v)        { this.tenantId = v; }
    public String getMemberId()            { return memberId; }
    public void setMemberId(String v)      { this.memberId = v; }
    public long getAvailablePoints()       { return availablePoints; }
    public void setAvailablePoints(long v) { this.availablePoints = v; }
    public long getLifetimeEarned()        { return lifetimeEarned; }
    public void setLifetimeEarned(long v)  { this.lifetimeEarned = v; }
    public long getLifetimeSpent()         { return lifetimeSpent; }
    public void setLifetimeSpent(long v)   { this.lifetimeSpent = v; }
}
