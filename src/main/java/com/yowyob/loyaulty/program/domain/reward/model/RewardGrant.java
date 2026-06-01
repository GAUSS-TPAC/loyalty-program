package com.yowyob.loyaulty.program.domain.reward.model;

import com.yowyob.loyaulty.program.domain.reward.model.enums.GrantStatus;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.UUID;

public class RewardGrant {

    private final UUID id;
    private final TenantId tenantId;
    private final String memberId;
    private final UUID rewardId;
    private GrantStatus status;
    private final Instant grantedAt;
    private Instant expiresAt;
    private Instant usedAt;
    private String useContext;

    private RewardGrant(UUID id, TenantId tenantId, String memberId, UUID rewardId,
                        GrantStatus status, Instant grantedAt,
                        Instant expiresAt, Instant usedAt, String useContext) {
        this.id = id;
        this.tenantId = tenantId;
        this.memberId = memberId;
        this.rewardId = rewardId;
        this.status = status;
        this.grantedAt = grantedAt;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.useContext = useContext;
    }

    public static RewardGrant create(TenantId tenantId, String memberId,
                                      UUID rewardId, Instant expiresAt) {
        return new RewardGrant(UUID.randomUUID(), tenantId, memberId, rewardId,
                GrantStatus.ACTIVE, Instant.now(), expiresAt, null, null);
    }

    public static RewardGrant reconstitute(UUID id, TenantId tenantId, String memberId,
                                            UUID rewardId, GrantStatus status,
                                            Instant grantedAt, Instant expiresAt,
                                            Instant usedAt, String useContext) {
        return new RewardGrant(id, tenantId, memberId, rewardId, status,
                grantedAt, expiresAt, usedAt, useContext);
    }

    public void consume(String context) {
        if (status != GrantStatus.ACTIVE) {
            throw new IllegalStateException("Grant is not active: " + status);
        }
        if (expiresAt != null && Instant.now().isAfter(expiresAt)) {
            this.status = GrantStatus.EXPIRED;
            throw new IllegalStateException("Grant has expired");
        }
        this.status = GrantStatus.USED;
        this.usedAt = Instant.now();
        this.useContext = context;
    }

    public boolean isActive() {
        if (status != GrantStatus.ACTIVE) return false;
        if (expiresAt != null && Instant.now().isAfter(expiresAt)) {
            this.status = GrantStatus.EXPIRED;
            return false;
        }
        return true;
    }

    public UUID getId()             { return id; }
    public TenantId getTenantId()   { return tenantId; }
    public String getMemberId()     { return memberId; }
    public UUID getRewardId()       { return rewardId; }
    public GrantStatus getStatus()  { return status; }
    public Instant getGrantedAt()   { return grantedAt; }
    public Instant getExpiresAt()   { return expiresAt; }
    public Instant getUsedAt()      { return usedAt; }
    public String getUseContext()   { return useContext; }
}
