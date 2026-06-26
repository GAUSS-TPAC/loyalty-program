package com.yowyob.loyalty.api.reward.dto.response;

import com.yowyob.loyalty.domain.reward.model.Reward;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RewardResponse(
        UUID id,
        String name,
        String description,
        String type,
        BigDecimal numericValue,
        String valueUnit,
        int maxApplicationCount,
        long costInPoints,
        Integer stockRemaining,
        Instant validFrom,
        Instant validUntil,
        int grantExpiryDays,
        String imageUrl,
        String status,
        Instant createdAt
) {
    public static RewardResponse from(Reward reward) {
        return new RewardResponse(
                reward.id(),
                reward.name(),
                reward.description(),
                reward.type().name(),
                reward.value().numericValue(),
                reward.value().unit(),
                reward.value().maxApplicationCount(),
                reward.costInPoints(),
                reward.stockRemaining(),
                reward.validFrom(),
                reward.validUntil(),
                reward.grantExpiryDays(),
                reward.imageUrl(),
                reward.status().name(),
                reward.createdAt()
        );
    }
}
