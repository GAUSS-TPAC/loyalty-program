package com.yowyob.loyaulty.program.infrastructure.persistence.member.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("points_transactions")
public class PointsTransactionEntity {
    @Id private UUID id;
    @Column("tenant_id")        private UUID tenantId;
    @Column("member_id")        private String memberId;
    @Column("type")             private String type;
    @Column("amount")           private long amount;
    @Column("balance_before")   private long balanceBefore;
    @Column("balance_after")    private long balanceAfter;
    @Column("description")      private String description;
    @Column("source_reference") private String sourceReference;
    @Column("occurred_at")      private Instant occurredAt;

    public UUID getId()                   { return id; }
    public void setId(UUID v)             { this.id = v; }
    public UUID getTenantId()             { return tenantId; }
    public void setTenantId(UUID v)       { this.tenantId = v; }
    public String getMemberId()           { return memberId; }
    public void setMemberId(String v)     { this.memberId = v; }
    public String getType()               { return type; }
    public void setType(String v)         { this.type = v; }
    public long getAmount()               { return amount; }
    public void setAmount(long v)         { this.amount = v; }
    public long getBalanceBefore()        { return balanceBefore; }
    public void setBalanceBefore(long v)  { this.balanceBefore = v; }
    public long getBalanceAfter()         { return balanceAfter; }
    public void setBalanceAfter(long v)   { this.balanceAfter = v; }
    public String getDescription()        { return description; }
    public void setDescription(String v)  { this.description = v; }
    public String getSourceReference()    { return sourceReference; }
    public void setSourceReference(String v) { this.sourceReference = v; }
    public Instant getOccurredAt()        { return occurredAt; }
    public void setOccurredAt(Instant v)  { this.occurredAt = v; }
}
