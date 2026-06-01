package com.yowyob.loyaulty.program.domain.member.model;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;

import java.util.UUID;

public class PointsAccount {

    private final UUID id;
    private final TenantId tenantId;
    private final String memberId;
    private long availablePoints;
    private long lifetimeEarned;
    private long lifetimeSpent;

    private PointsAccount(UUID id, TenantId tenantId, String memberId,
                          long availablePoints, long lifetimeEarned, long lifetimeSpent) {
        this.id = id;
        this.tenantId = tenantId;
        this.memberId = memberId;
        this.availablePoints = availablePoints;
        this.lifetimeEarned = lifetimeEarned;
        this.lifetimeSpent = lifetimeSpent;
    }

    public static PointsAccount create(TenantId tenantId, String memberId) {
        return new PointsAccount(UUID.randomUUID(), tenantId, memberId, 0L, 0L, 0L);
    }

    public static PointsAccount reconstitute(UUID id, TenantId tenantId, String memberId,
                                              long availablePoints, long lifetimeEarned,
                                              long lifetimeSpent) {
        return new PointsAccount(id, tenantId, memberId, availablePoints, lifetimeEarned, lifetimeSpent);
    }

    /**
     * Crédite des points et retourne la transaction immuable.
     * Le multiplicateur de palier est appliqué par l'appelant avant de passer {@code amount}.
     */
    public PointsTransaction earn(long amount, String description, String sourceRef) {
        if (amount <= 0) throw new IllegalArgumentException("Earned amount must be positive");
        long before = availablePoints;
        availablePoints += amount;
        lifetimeEarned += amount;
        return PointsTransaction.earn(tenantId, memberId, amount, before, description, sourceRef);
    }

    /**
     * Débite des points pour une rédemption et retourne la transaction.
     */
    public PointsTransaction spend(long amount, String description, String sourceRef) {
        if (amount <= 0) throw new IllegalArgumentException("Spent amount must be positive");
        if (availablePoints < amount) {
            throw new IllegalArgumentException(
                    "Insufficient points: available=" + availablePoints + ", requested=" + amount);
        }
        long before = availablePoints;
        availablePoints -= amount;
        lifetimeSpent += amount;
        return PointsTransaction.spend(tenantId, memberId, amount, before, description, sourceRef);
    }

    /**
     * Expire un certain nombre de points (appelé par le scheduler).
     */
    public PointsTransaction expire(long amount) {
        if (amount <= 0) throw new IllegalArgumentException("Expired amount must be positive");
        long toExpire = Math.min(amount, availablePoints);
        long before = availablePoints;
        availablePoints -= toExpire;
        return PointsTransaction.expire(tenantId, memberId, toExpire, before);
    }

    public boolean canSpend(long amount) {
        return availablePoints >= amount;
    }

    public UUID getId()              { return id; }
    public TenantId getTenantId()    { return tenantId; }
    public String getMemberId()      { return memberId; }
    public long getAvailablePoints() { return availablePoints; }
    public long getLifetimeEarned()  { return lifetimeEarned; }
    public long getLifetimeSpent()   { return lifetimeSpent; }
}
