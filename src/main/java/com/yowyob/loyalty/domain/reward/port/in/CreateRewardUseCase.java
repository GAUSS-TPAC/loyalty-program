package com.yowyob.loyalty.domain.reward.port.in;

import com.yowyob.loyalty.domain.reward.model.Reward;
import com.yowyob.loyalty.domain.reward.model.RewardType;
import com.yowyob.loyalty.domain.reward.model.RewardValue;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

public interface CreateRewardUseCase {
    Mono<Reward> createReward(
            TenantId tenantId,
            String name,
            String description,
            RewardType type,
            RewardValue value,
            long costInPoints,
            Integer stockTotal,
            Instant validFrom,
            Instant validUntil,
            int grantExpiryDays,
            String imageUrl,
            Map<String, Object> metadata,
            String idempotencyKey
    );
}
