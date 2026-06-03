package com.yowyob.loyalty.domain.loyalty.model.points;

import com.yowyob.loyalty.domain.loyalty.exception.LoyaltyDomainException;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.time.Instant;
import java.util.UUID;

public class PointsAccount {

    private final UUID id;
    private final TenantId tenantId;
    private final UserId memberId;
    private final long availablePoints;
    private final long lifetimeEarned;
    private final long lifetimeSpent;
    private final long version;
    private final Instant lastActivityAt;
    private final Instant updatedAt;

    private PointsAccount(UUID id, TenantId tenantId, UserId memberId, long availablePoints,
                          long lifetimeEarned, long lifetimeSpent, long version,
                          Instant lastActivityAt, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.memberId = memberId;
        this.availablePoints = availablePoints;
        this.lifetimeEarned = lifetimeEarned;
        this.lifetimeSpent = lifetimeSpent;
        this.version = version;
        this.lastActivityAt = lastActivityAt;
        this.updatedAt = updatedAt;
    }

    public static PointsAccount create(UUID id, TenantId tenantId, UserId memberId) {
        Instant now = Instant.now();
        return new PointsAccount(id, tenantId, memberId, 0, 0, 0, 0, null, now);
    }

    public static PointsAccount reconstruct(UUID id, TenantId tenantId, UserId memberId, long availablePoints,
                                            long lifetimeEarned, long lifetimeSpent, long version,
                                            Instant lastActivityAt, Instant updatedAt) {
        return new PointsAccount(id, tenantId, memberId, availablePoints, lifetimeEarned, lifetimeSpent,
                version, lastActivityAt, updatedAt);
    }

    public PointsAccount earn(long points) {
        if (points <= 0) {
            throw new LoyaltyDomainException("Points to earn must be greater than 0");
        }
        Instant now = Instant.now();
        return new PointsAccount(id, tenantId, memberId, availablePoints + points,
                lifetimeEarned + points, lifetimeSpent, version + 1, now, now);
    }

    public PointsAccount spend(long points) {
        if (points <= 0) {
            throw new LoyaltyDomainException("Points to spend must be greater than 0");
        }
        if (!hasEnoughPoints(points)) {
            throw new LoyaltyDomainException("Solde de points insuffisant");
        }
        Instant now = Instant.now();
        return new PointsAccount(id, tenantId, memberId, availablePoints - points,
                lifetimeEarned, lifetimeSpent + points, version + 1, now, now);
    }

    public PointsAccount expire(long points) {
        if (points <= 0) {
            throw new LoyaltyDomainException("Points to expire must be greater than 0");
        }
        if (!hasEnoughPoints(points)) {
            throw new LoyaltyDomainException("Cannot expire more points than available");
        }
        Instant now = Instant.now();
        // Expiry doesn't increase lifetimeSpent according to the spec
        return new PointsAccount(id, tenantId, memberId, availablePoints - points,
                lifetimeEarned, lifetimeSpent, version + 1, now, now);
    }

    public boolean hasEnoughPoints(long required) {
        return availablePoints >= required;
    }

    public UUID getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public UserId getMemberId() {
        return memberId;
    }

    public long getAvailablePoints() {
        return availablePoints;
    }

    public long getLifetimeEarned() {
        return lifetimeEarned;
    }

    public long getLifetimeSpent() {
        return lifetimeSpent;
    }

    public long getVersion() {
        return version;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
