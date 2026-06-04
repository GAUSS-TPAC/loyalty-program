package com.yowyob.loyalty.api.loyalty.dto.response;

import com.yowyob.loyalty.domain.loyalty.model.points.PointsTransaction;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record PointsTransactionResponse(
        UUID id,
        String type,
        long amount,
        long balanceAfter,
        String source,
        UUID ruleId,
        Instant createdAt,
        Map<String, Object> metadata
) {
    public static PointsTransactionResponse from(PointsTransaction tx) {
        return new PointsTransactionResponse(
                tx.id(),
                tx.type(),
                tx.amount(),
                tx.balanceAfter(),
                tx.source(),
                tx.ruleId(),
                tx.createdAt(),
                tx.metadata()
        );
    }
}
