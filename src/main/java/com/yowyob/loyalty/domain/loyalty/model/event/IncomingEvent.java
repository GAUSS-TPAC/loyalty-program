package com.yowyob.loyalty.domain.loyalty.model.event;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public record IncomingEvent(
        String eventType,
        TenantId tenantId,
        UserId memberId,
        String idempotencyKey,
        Instant occurredAt,
        Map<String, Object> payload
) {
    public Optional<Object> getPayloadValue(String key) {
        if (payload == null) return Optional.empty();
        return Optional.ofNullable(payload.get(key));
    }

    public Optional<String> getPayloadString(String key) {
        return getPayloadValue(key).map(Object::toString);
    }

    public Optional<BigDecimal> getPayloadDecimal(String key) {
        return getPayloadValue(key).map(val -> {
            if (val instanceof BigDecimal bd) {
                return bd;
            }
            if (val instanceof Number num) {
                return BigDecimal.valueOf(num.doubleValue());
            }
            try {
                return new BigDecimal(val.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        });
    }
}
