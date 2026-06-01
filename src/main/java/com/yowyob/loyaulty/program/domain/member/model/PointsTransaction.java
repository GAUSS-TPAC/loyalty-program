package com.yowyob.loyaulty.program.domain.member.model;

import com.yowyob.loyaulty.program.domain.member.model.enums.PointsTransactionType;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.UUID;

public final class PointsTransaction {

    private final UUID id;
    private final TenantId tenantId;
    private final String memberId;
    private final PointsTransactionType type;
    private final long amount;
    private final long balanceBefore;
    private final long balanceAfter;
    private final String description;
    private final String sourceReference;
    private final Instant occurredAt;

    private PointsTransaction(UUID id, TenantId tenantId, String memberId,
                               PointsTransactionType type, long amount,
                               long balanceBefore, long balanceAfter,
                               String description, String sourceReference,
                               Instant occurredAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.memberId = memberId;
        this.type = type;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.sourceReference = sourceReference;
        this.occurredAt = occurredAt;
    }

    public static PointsTransaction earn(TenantId tenantId, String memberId,
                                          long amount, long balanceBefore,
                                          String description, String sourceRef) {
        return new PointsTransaction(UUID.randomUUID(), tenantId, memberId,
                PointsTransactionType.EARN, amount, balanceBefore,
                balanceBefore + amount, description, sourceRef, Instant.now());
    }

    public static PointsTransaction spend(TenantId tenantId, String memberId,
                                           long amount, long balanceBefore,
                                           String description, String sourceRef) {
        return new PointsTransaction(UUID.randomUUID(), tenantId, memberId,
                PointsTransactionType.SPEND, amount, balanceBefore,
                balanceBefore - amount, description, sourceRef, Instant.now());
    }

    public static PointsTransaction expire(TenantId tenantId, String memberId,
                                            long amount, long balanceBefore) {
        return new PointsTransaction(UUID.randomUUID(), tenantId, memberId,
                PointsTransactionType.EXPIRE, amount, balanceBefore,
                balanceBefore - amount, "Points expired", null, Instant.now());
    }

    public static PointsTransaction reconstitute(UUID id, TenantId tenantId, String memberId,
                                                  PointsTransactionType type, long amount,
                                                  long balanceBefore, long balanceAfter,
                                                  String description, String sourceReference,
                                                  Instant occurredAt) {
        return new PointsTransaction(id, tenantId, memberId, type, amount,
                balanceBefore, balanceAfter, description, sourceReference, occurredAt);
    }

    public UUID getId()                      { return id; }
    public TenantId getTenantId()            { return tenantId; }
    public String getMemberId()              { return memberId; }
    public PointsTransactionType getType()   { return type; }
    public long getAmount()                  { return amount; }
    public long getBalanceBefore()           { return balanceBefore; }
    public long getBalanceAfter()            { return balanceAfter; }
    public String getDescription()           { return description; }
    public String getSourceReference()       { return sourceReference; }
    public Instant getOccurredAt()           { return occurredAt; }
}
