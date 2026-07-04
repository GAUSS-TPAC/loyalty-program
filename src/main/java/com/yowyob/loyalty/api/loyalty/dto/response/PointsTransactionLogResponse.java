package com.yowyob.loyalty.api.loyalty.dto.response;

import com.yowyob.loyalty.domain.loyalty.model.points.PointsTransaction;

import java.time.Instant;
import java.util.UUID;

public record PointsTransactionLogResponse(
        UUID id,
        UUID pointsAccountId,
        String type,
        long amount,
        long balanceAfter,
        String source,
        UUID ruleId,
        Instant createdAt
) {
    public static PointsTransactionLogResponse from(PointsTransaction tx) {
        return new PointsTransactionLogResponse(
                tx.id(),
                tx.pointsAccountId(),
                tx.type(),
                tx.amount(),
                tx.balanceAfter(),
                tx.source(),
                tx.ruleId(),
                tx.createdAt()
        );
    }
}
