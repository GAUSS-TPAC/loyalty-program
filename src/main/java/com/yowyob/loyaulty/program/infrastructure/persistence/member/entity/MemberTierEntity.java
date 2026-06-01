package com.yowyob.loyaulty.program.infrastructure.persistence.member.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("member_tiers")
public class MemberTierEntity {
    @Id private UUID id;
    @Column("tenant_id")       private UUID tenantId;
    @Column("member_id")       private String memberId;
    @Column("level")           private String level;
    @Column("lifetime_points") private long lifetimePoints;
    @Column("reached_at")      private Instant reachedAt;

    public UUID getId()                { return id; }
    public void setId(UUID v)          { this.id = v; }
    public UUID getTenantId()          { return tenantId; }
    public void setTenantId(UUID v)    { this.tenantId = v; }
    public String getMemberId()        { return memberId; }
    public void setMemberId(String v)  { this.memberId = v; }
    public String getLevel()           { return level; }
    public void setLevel(String v)     { this.level = v; }
    public long getLifetimePoints()    { return lifetimePoints; }
    public void setLifetimePoints(long v) { this.lifetimePoints = v; }
    public Instant getReachedAt()      { return reachedAt; }
    public void setReachedAt(Instant v){ this.reachedAt = v; }
}
