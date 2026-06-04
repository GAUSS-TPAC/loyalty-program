package com.yowyob.loyalty.infrastructure.persistence.loyalty.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("member_tiers")
public class MemberTierEntity {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID memberId;
    private String tierLevel;
    private BigDecimal multiplier;
    private Instant reachedAt;
    private Instant validUntil;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getMemberId() { return memberId; }
    public void setMemberId(UUID memberId) { this.memberId = memberId; }
    public String getTierLevel() { return tierLevel; }
    public void setTierLevel(String tierLevel) { this.tierLevel = tierLevel; }
    public BigDecimal getMultiplier() { return multiplier; }
    public void setMultiplier(BigDecimal multiplier) { this.multiplier = multiplier; }
    public Instant getReachedAt() { return reachedAt; }
    public void setReachedAt(Instant reachedAt) { this.reachedAt = reachedAt; }
    public Instant getValidUntil() { return validUntil; }
    public void setValidUntil(Instant validUntil) { this.validUntil = validUntil; }
}
