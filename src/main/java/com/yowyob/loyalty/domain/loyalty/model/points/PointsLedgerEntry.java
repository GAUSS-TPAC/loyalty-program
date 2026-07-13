package com.yowyob.loyalty.domain.loyalty.model.points;

import com.yowyob.loyalty.domain.shared.model.UserId;

public record PointsLedgerEntry(PointsTransaction transaction, UserId memberId) {
    public String reason() {
        if (transaction.metadata() == null) return null;
        Object reason = transaction.metadata().get("reason");
        return reason != null ? reason.toString() : null;
    }
}
