package com.yowyob.loyalty.domain.referral.model;

import com.yowyob.loyalty.domain.referral.exception.ReferralDomainException;
import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.UUID;

public class ReferralProgram {

    private final UUID id;
    private final TenantId tenantId;
    private String name;
    private boolean active;
    private int maxReferralsPerReferrer;
    private int referralWindowDays;
    private UUID referrerRewardId;
    private UUID refereeRewardId;
    private int minConversionAmount;
    private final Instant createdAt;
    private Instant updatedAt;

    private ReferralProgram(UUID id, TenantId tenantId, String name, boolean active,
                             int maxReferralsPerReferrer, int referralWindowDays,
                             UUID referrerRewardId, UUID refereeRewardId,
                             int minConversionAmount, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.active = active;
        this.maxReferralsPerReferrer = maxReferralsPerReferrer;
        this.referralWindowDays = referralWindowDays;
        this.referrerRewardId = referrerRewardId;
        this.refereeRewardId = refereeRewardId;
        this.minConversionAmount = minConversionAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ReferralProgram create(UUID id, TenantId tenantId, String name,
                                          int maxReferrals, int windowDays,
                                          UUID referrerRewardId, UUID refereeRewardId,
                                          int minConversionAmount) {
        if (name == null || name.isBlank())
            throw new ReferralDomainException("Le nom du programme est obligatoire");
        Instant now = Instant.now();
        return new ReferralProgram(id, tenantId, name, false, maxReferrals, windowDays,
                referrerRewardId, refereeRewardId, minConversionAmount, now, now);
    }

    public static ReferralProgram reconstruct(UUID id, TenantId tenantId, String name, boolean active,
                                               int maxReferrals, int windowDays,
                                               UUID referrerRewardId, UUID refereeRewardId,
                                               int minConversionAmount, Instant createdAt, Instant updatedAt) {
        return new ReferralProgram(id, tenantId, name, active, maxReferrals, windowDays,
                referrerRewardId, refereeRewardId, minConversionAmount, createdAt, updatedAt);
    }

    public ReferralProgram activate() {
        this.active = true;
        this.updatedAt = Instant.now();
        return this;
    }

    public ReferralProgram deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
        return this;
    }

    public ReferralProgram update(String name, int maxReferrals, int windowDays, int minConversionAmount) {
        if (name != null && !name.isBlank()) this.name = name;
        this.maxReferralsPerReferrer = maxReferrals;
        this.referralWindowDays = windowDays;
        this.minConversionAmount = minConversionAmount;
        this.updatedAt = Instant.now();
        return this;
    }

    public UUID id() { return id; }
    public TenantId tenantId() { return tenantId; }
    public String name() { return name; }
    public boolean isActive() { return active; }
    public int maxReferralsPerReferrer() { return maxReferralsPerReferrer; }
    public int referralWindowDays() { return referralWindowDays; }
    public UUID referrerRewardId() { return referrerRewardId; }
    public UUID refereeRewardId() { return refereeRewardId; }
    public int minConversionAmount() { return minConversionAmount; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
