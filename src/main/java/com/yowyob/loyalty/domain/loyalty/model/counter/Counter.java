package com.yowyob.loyalty.domain.loyalty.model.counter;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record Counter(
        UUID id,
        TenantId tenantId,
        UserId memberId,
        String counterKey,
        long value,
        String windowType,
        Instant windowStart,
        Instant updatedAt
) {
    public Counter increment(long delta) {
        return new Counter(id, tenantId, memberId, counterKey, value + delta,
                windowType, windowStart, Instant.now());
    }

    public Counter reset() {
        return new Counter(id, tenantId, memberId, counterKey, 0,
                windowType, Instant.now(), Instant.now());
    }

    public boolean isExpiredWindow(Instant now) {
        if (windowType == null || "LIFETIME".equals(windowType)) {
            return false;
        }
        if (windowStart == null) {
            return true; // Technically shouldn't happen, but safe fallback
        }

        ZonedDateTime start = windowStart.atZone(ZoneOffset.UTC);
        ZonedDateTime current = now.atZone(ZoneOffset.UTC);

        return switch (windowType) {
            case "DAILY" -> !start.toLocalDate().equals(current.toLocalDate());
            case "WEEKLY" -> {
                long weeksBetween = ChronoUnit.WEEKS.between(
                        start.with(java.time.DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS),
                        current.with(java.time.DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS)
                );
                yield weeksBetween > 0;
            }
            case "MONTHLY" -> !start.withDayOfMonth(1).toLocalDate().equals(current.withDayOfMonth(1).toLocalDate());
            default -> false;
        };
    }
}
