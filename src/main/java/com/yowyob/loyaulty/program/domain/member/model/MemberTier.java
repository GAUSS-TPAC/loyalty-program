package com.yowyob.loyaulty.program.domain.member.model;

import com.yowyob.loyaulty.program.domain.member.model.enums.TierLevel;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.UUID;

public class MemberTier {

    private final UUID id;
    private final TenantId tenantId;
    private final String memberId;
    private TierLevel level;
    private long lifetimePoints;
    private final Instant reachedAt;

    private MemberTier(UUID id, TenantId tenantId, String memberId,
                       TierLevel level, long lifetimePoints, Instant reachedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.memberId = memberId;
        this.level = level;
        this.lifetimePoints = lifetimePoints;
        this.reachedAt = reachedAt;
    }

    public static MemberTier create(TenantId tenantId, String memberId) {
        return new MemberTier(UUID.randomUUID(), tenantId, memberId,
                TierLevel.BRONZE, 0L, Instant.now());
    }

    public static MemberTier reconstitute(UUID id, TenantId tenantId, String memberId,
                                          TierLevel level, long lifetimePoints, Instant reachedAt) {
        return new MemberTier(id, tenantId, memberId, level, lifetimePoints, reachedAt);
    }

    /**
     * Ajoute des points à vie et recalcule le palier.
     * Retourne true si le palier a changé.
     */
    public boolean addPoints(long points) {
        this.lifetimePoints += points;
        TierLevel newLevel = calculateLevel(lifetimePoints);
        if (newLevel != this.level) {
            this.level = newLevel;
            return true;
        }
        return false;
    }

    private TierLevel calculateLevel(long pts) {
        if (pts >= 20_000) return TierLevel.PLATINUM;
        if (pts >= 5_000)  return TierLevel.GOLD;
        if (pts >= 1_000)  return TierLevel.SILVER;
        return TierLevel.BRONZE;
    }

    public long pointsToNextTier() {
        return switch (level) {
            case BRONZE   -> 1_000  - lifetimePoints;
            case SILVER   -> 5_000  - lifetimePoints;
            case GOLD     -> 20_000 - lifetimePoints;
            case PLATINUM -> 0;
        };
    }

    public double getMultiplier() { return level.getPointsMultiplier(); }

    public UUID getId()            { return id; }
    public TenantId getTenantId()  { return tenantId; }
    public String getMemberId()    { return memberId; }
    public TierLevel getLevel()    { return level; }
    public long getLifetimePoints(){ return lifetimePoints; }
    public Instant getReachedAt()  { return reachedAt; }
}
