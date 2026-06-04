package com.yowyob.loyalty.infrastructure.persistence.loyalty.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("points_transactions")
public class PointsTransactionEntity {

    @Id
    private UUID id;
    private UUID pointsAccountId;
    private UUID tenantId;
    private String type;
    private long amount;
    private long balanceAfter;
    private String source;
    private UUID ruleId;
    private String eventIdempotencyKey;
    private String metadata;
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPointsAccountId() { return pointsAccountId; }
    public void setPointsAccountId(UUID pointsAccountId) { this.pointsAccountId = pointsAccountId; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public long getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(long balanceAfter) { this.balanceAfter = balanceAfter; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public UUID getRuleId() { return ruleId; }
    public void setRuleId(UUID ruleId) { this.ruleId = ruleId; }
    public String getEventIdempotencyKey() { return eventIdempotencyKey; }
    public void setEventIdempotencyKey(String eventIdempotencyKey) { this.eventIdempotencyKey = eventIdempotencyKey; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
