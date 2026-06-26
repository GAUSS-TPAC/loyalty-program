package com.yowyob.loyalty.domain.reward.model;

import com.yowyob.loyalty.domain.reward.exception.GrantAlreadyUsedException;
import com.yowyob.loyalty.domain.reward.exception.GrantExpiredException;
import com.yowyob.loyalty.domain.reward.exception.RewardDomainException;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class RewardGrant {

    private final UUID id;
    private final TenantId tenantId;
    private final UserId memberId;
    private final UUID rewardId;
    private final String rewardName;
    private final RewardType rewardType;
    private final RewardValue rewardValue;
    private final GrantSource source;
    private final UUID sourceRuleId;
    private final String sourceEventId;
    private GrantStatus status;
    private int remainingApplications;
    private final Instant grantedAt;
    private final Instant expiresAt;
    private Instant usedAt;
    private String usedInContext;
    private int version;

    private RewardGrant(UUID id, TenantId tenantId, UserId memberId, UUID rewardId,
                        String rewardName, RewardType rewardType, RewardValue rewardValue,
                        GrantSource source, UUID sourceRuleId, String sourceEventId,
                        GrantStatus status, int remainingApplications,
                        Instant grantedAt, Instant expiresAt,
                        Instant usedAt, String usedInContext, int version) {
        this.id = id;
        this.tenantId = tenantId;
        this.memberId = memberId;
        this.rewardId = rewardId;
        this.rewardName = rewardName;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
        this.source = source;
        this.sourceRuleId = sourceRuleId;
        this.sourceEventId = sourceEventId;
        this.status = status;
        this.remainingApplications = remainingApplications;
        this.grantedAt = grantedAt;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.usedInContext = usedInContext;
        this.version = version;
    }

    public static RewardGrant create(UUID id, TenantId tenantId, UserId memberId,
                                     Reward reward, GrantSource source,
                                     UUID sourceRuleId, String sourceEventId) {
        Instant now = Instant.now();
        Instant expiresAt = reward.grantExpiryDays() > 0
                ? now.plus(reward.grantExpiryDays(), ChronoUnit.DAYS)
                : null;

        return new RewardGrant(id, tenantId, memberId,
                reward.id(), reward.name(), reward.type(), reward.value(),
                source, sourceRuleId, sourceEventId,
                GrantStatus.PENDING, reward.value().maxApplicationCount(),
                now, expiresAt, null, null, 0);
    }

    public static RewardGrant reconstruct(UUID id, TenantId tenantId, UserId memberId,
                                          UUID rewardId, String rewardName, RewardType rewardType,
                                          RewardValue rewardValue, GrantSource source,
                                          UUID sourceRuleId, String sourceEventId,
                                          GrantStatus status, int remainingApplications,
                                          Instant grantedAt, Instant expiresAt,
                                          Instant usedAt, String usedInContext, int version) {
        return new RewardGrant(id, tenantId, memberId, rewardId, rewardName, rewardType,
                rewardValue, source, sourceRuleId, sourceEventId,
                status, remainingApplications, grantedAt, expiresAt, usedAt, usedInContext, version);
    }

    public RewardGrant activate() {
        if (!status.canTransitionTo(GrantStatus.ACTIVE))
            throw new RewardDomainException("Impossible d'activer le grant depuis l'état " + status);
        this.status = GrantStatus.ACTIVE;
        this.version++;
        return this;
    }

    public RewardGrant consume(String context) {
        if (!status.isUsable())
            throw new GrantAlreadyUsedException(id);
        if (isExpired())
            throw new GrantExpiredException(id, expiresAt);

        this.remainingApplications--;
        if (this.remainingApplications == 0) {
            this.status = GrantStatus.USED;
        }
        this.usedAt = Instant.now();
        this.usedInContext = context;
        this.version++;
        return this;
    }

    public RewardGrant expire() {
        if (!status.canTransitionTo(GrantStatus.EXPIRED))
            throw new RewardDomainException("Impossible d'expirer le grant depuis l'état " + status);
        this.status = GrantStatus.EXPIRED;
        this.version++;
        return this;
    }

    public RewardGrant reverse(String reason) {
        if (!status.canTransitionTo(GrantStatus.REVERSED))
            throw new RewardDomainException("Impossible d'inverser le grant depuis l'état " + status);
        this.status = GrantStatus.REVERSED;
        this.version++;
        return this;
    }

    public RewardGrant cancel(String reason) {
        if (!status.canTransitionTo(GrantStatus.CANCELLED))
            throw new RewardDomainException("Impossible d'annuler le grant depuis l'état " + status);
        this.status = GrantStatus.CANCELLED;
        this.version++;
        return this;
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public UUID id() { return id; }
    public TenantId tenantId() { return tenantId; }
    public UserId memberId() { return memberId; }
    public UUID rewardId() { return rewardId; }
    public String rewardName() { return rewardName; }
    public RewardType rewardType() { return rewardType; }
    public RewardValue rewardValue() { return rewardValue; }
    public GrantSource source() { return source; }
    public UUID sourceRuleId() { return sourceRuleId; }
    public String sourceEventId() { return sourceEventId; }
    public GrantStatus status() { return status; }
    public int remainingApplications() { return remainingApplications; }
    public Instant grantedAt() { return grantedAt; }
    public Instant expiresAt() { return expiresAt; }
    public Instant usedAt() { return usedAt; }
    public String usedInContext() { return usedInContext; }
    public int version() { return version; }
}
