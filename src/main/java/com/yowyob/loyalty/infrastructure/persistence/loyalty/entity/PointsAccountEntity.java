package com.yowyob.loyalty.infrastructure.persistence.loyalty.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("points_accounts")
public class PointsAccountEntity {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID memberId;
    private long availablePoints;
    private long lifetimeEarned;
    private long lifetimeSpent;
    private long version;
    private Instant lastActivityAt;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getMemberId() { return memberId; }
    public void setMemberId(UUID memberId) { this.memberId = memberId; }
    public long getAvailablePoints() { return availablePoints; }
    public void setAvailablePoints(long availablePoints) { this.availablePoints = availablePoints; }
    public long getLifetimeEarned() { return lifetimeEarned; }
    public void setLifetimeEarned(long lifetimeEarned) { this.lifetimeEarned = lifetimeEarned; }
    public long getLifetimeSpent() { return lifetimeSpent; }
    public void setLifetimeSpent(long lifetimeSpent) { this.lifetimeSpent = lifetimeSpent; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
    public Instant getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(Instant lastActivityAt) { this.lastActivityAt = lastActivityAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
