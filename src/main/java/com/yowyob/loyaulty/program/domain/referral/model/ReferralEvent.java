package com.yowyob.loyaulty.program.domain.referral.model;

import com.yowyob.loyaulty.program.domain.referral.model.enums.ReferralStatus;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.UUID;

/**
 * Enregistrement concret d'un parrainage : qui a parrainé qui, quel statut.
 */
public class ReferralEvent {

    private final UUID id;
    private final TenantId tenantId;
    private final String referrerId;
    private final String refereeId;
    private final String referralCode;
    private ReferralStatus status;
    private final Instant enrolledAt;
    private Instant convertedAt;
    private Instant rewardedAt;
    private Instant expiresAt;

    private ReferralEvent(UUID id, TenantId tenantId, String referrerId, String refereeId,
                          String referralCode, ReferralStatus status,
                          Instant enrolledAt, Instant convertedAt, Instant rewardedAt, Instant expiresAt) {
        this.id          = id;
        this.tenantId    = tenantId;
        this.referrerId  = referrerId;
        this.refereeId   = refereeId;
        this.referralCode = referralCode;
        this.status      = status;
        this.enrolledAt  = enrolledAt;
        this.convertedAt = convertedAt;
        this.rewardedAt  = rewardedAt;
        this.expiresAt   = expiresAt;
    }

    public static ReferralEvent create(TenantId tenantId, String referrerId,
                                        String refereeId, String referralCode,
                                        int deadlineDays) {
        Instant now = Instant.now();
        return new ReferralEvent(UUID.randomUUID(), tenantId, referrerId, refereeId,
                referralCode, ReferralStatus.PENDING,
                now, null, null,
                now.plusSeconds((long) deadlineDays * 86400));
    }

    public static ReferralEvent reconstitute(UUID id, TenantId tenantId,
                                              String referrerId, String refereeId, String referralCode,
                                              ReferralStatus status, Instant enrolledAt,
                                              Instant convertedAt, Instant rewardedAt, Instant expiresAt) {
        return new ReferralEvent(id, tenantId, referrerId, refereeId, referralCode,
                status, enrolledAt, convertedAt, rewardedAt, expiresAt);
    }

    // ── Transitions ───────────────────────────────────────────────────────

    public void convert() {
        if (status != ReferralStatus.PENDING) throw new IllegalStateException("Already converted or expired");
        this.status = ReferralStatus.CONVERTED;
        this.convertedAt = Instant.now();
    }

    public void reward() {
        if (status != ReferralStatus.CONVERTED) throw new IllegalStateException("Must be converted first");
        this.status = ReferralStatus.REWARDED;
        this.rewardedAt = Instant.now();
    }

    public void expire() {
        if (status == ReferralStatus.PENDING) {
            this.status = ReferralStatus.EXPIRED;
        }
    }

    public boolean isPending()  { return status == ReferralStatus.PENDING; }
    public boolean isExpired()  { return Instant.now().isAfter(expiresAt) && status == ReferralStatus.PENDING; }

    // ── Accesseurs ────────────────────────────────────────────────────────

    public UUID getId()              { return id; }
    public TenantId getTenantId()    { return tenantId; }
    public String getReferrerId()    { return referrerId; }
    public String getRefereeId()     { return refereeId; }
    public String getReferralCode()  { return referralCode; }
    public ReferralStatus getStatus() { return status; }
    public Instant getEnrolledAt()   { return enrolledAt; }
    public Instant getConvertedAt()  { return convertedAt; }
    public Instant getRewardedAt()   { return rewardedAt; }
    public Instant getExpiresAt()    { return expiresAt; }
}
