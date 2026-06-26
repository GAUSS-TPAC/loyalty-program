package com.yowyob.loyalty.domain.reward.port.in;

import com.yowyob.loyalty.domain.reward.model.Reward;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetRewardCatalogUseCase {
    Flux<Reward> getCatalog(TenantId tenantId, boolean activeOnly, int page, int size);
    Mono<Reward> getReward(TenantId tenantId, UUID rewardId);
}
