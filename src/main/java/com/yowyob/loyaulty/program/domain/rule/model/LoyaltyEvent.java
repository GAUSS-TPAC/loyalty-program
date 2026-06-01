package com.yowyob.loyaulty.program.domain.rule.model;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record LoyaltyEvent(
        UUID eventId,
        TenantId tenantId,
        String memberId,
        String eventType,
        Instant occurredAt,
        Map<String, Object> payload,
        String idempotencyKey
) {
    public BigDecimal amountFromPayload() {
        Object v = payload != null ? payload.get("amount") : null;
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}
