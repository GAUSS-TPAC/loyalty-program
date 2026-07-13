package com.yowyob.loyalty.api.loyalty.dto.response;

import com.yowyob.loyalty.domain.loyalty.model.points.PointsLedgerEntry;

import java.time.Instant;
import java.util.UUID;

public record PointsTransactionLogResponse(
        UUID id,
        UUID pointsAccountId,
        UUID memberId,
        String type,
        long amount,
        long balanceAfter,
        String source,
        UUID ruleId,
        String reason,
        Instant createdAt
) {
    public static PointsTransactionLogResponse from(PointsLedgerEntry entry) {
        var tx = entry.transaction();
        return new PointsTransactionLogResponse(
                tx.id(),
                tx.pointsAccountId(),
                entry.memberId() != null ? entry.memberId().value() : null,
                tx.type(),
                tx.amount(),
                tx.balanceAfter(),
                tx.source(),
                tx.ruleId(),
                entry.reason(),
                tx.createdAt()
        );
    }
}
