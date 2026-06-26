package com.yowyob.loyalty.domain.reward.port.in;

import com.yowyob.loyalty.domain.reward.model.Reward;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

public interface UpdateRewardUseCase {
    Mono<Reward> updateReward(TenantId tenantId, UUID rewardId, String name, String description, String imageUrl, Map<String, Object> metadata);
    Mono<Reward> activateReward(TenantId tenantId, UUID rewardId);
    Mono<Reward> pauseReward(TenantId tenantId, UUID rewardId);
    Mono<Reward> archiveReward(TenantId tenantId, UUID rewardId);
}
