package com.yowyob.loyalty.infrastructure.persistence.reward.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("reward_grants")
public class RewardGrantEntity {

    @Id
    private UUID id;
    @Column("tenant_id")
    private UUID tenantId;
    @Column("member_id")
    private UUID memberId;
    @Column("reward_id")
    private UUID rewardId;
    @Column("reward_name")
    private String rewardName;
    @Column("reward_type")
    private String rewardType;
    @Column("reward_value_json")
    private String rewardValueJson;
    private String source;
    @Column("source_rule_id")
    private UUID sourceRuleId;
    @Column("source_event_id")
    private String sourceEventId;
    private String status;
    @Column("remaining_applications")
    private int remainingApplications;
    @Column("granted_at")
    private Instant grantedAt;
    @Column("expires_at")
    private Instant expiresAt;
    @Column("used_at")
    private Instant usedAt;
    @Column("used_in_context")
    private String usedInContext;
    @Column("idempotency_key")
    private String idempotencyKey;
    private int version;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getMemberId() { return memberId; }
    public void setMemberId(UUID memberId) { this.memberId = memberId; }
    public UUID getRewardId() { return rewardId; }
    public void setRewardId(UUID rewardId) { this.rewardId = rewardId; }
    public String getRewardName() { return rewardName; }
    public void setRewardName(String rewardName) { this.rewardName = rewardName; }
    public String getRewardType() { return rewardType; }
    public void setRewardType(String rewardType) { this.rewardType = rewardType; }
    public String getRewardValueJson() { return rewardValueJson; }
    public void setRewardValueJson(String rewardValueJson) { this.rewardValueJson = rewardValueJson; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public UUID getSourceRuleId() { return sourceRuleId; }
    public void setSourceRuleId(UUID sourceRuleId) { this.sourceRuleId = sourceRuleId; }
    public String getSourceEventId() { return sourceEventId; }
    public void setSourceEventId(String sourceEventId) { this.sourceEventId = sourceEventId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getRemainingApplications() { return remainingApplications; }
    public void setRemainingApplications(int remainingApplications) { this.remainingApplications = remainingApplications; }
    public Instant getGrantedAt() { return grantedAt; }
    public void setGrantedAt(Instant grantedAt) { this.grantedAt = grantedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }
    public String getUsedInContext() { return usedInContext; }
    public void setUsedInContext(String usedInContext) { this.usedInContext = usedInContext; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}
