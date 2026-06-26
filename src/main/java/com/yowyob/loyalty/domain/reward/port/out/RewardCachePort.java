package com.yowyob.loyalty.domain.reward.port.out;

import com.yowyob.loyalty.domain.reward.model.Reward;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RewardCachePort {
    Mono<List<Reward>> getCachedCatalog(TenantId tenantId);
    Mono<Void> cacheCatalog(TenantId tenantId, List<Reward> rewards, Duration ttl);
    Mono<Void> evictCatalog(TenantId tenantId);
    Mono<Optional<Reward>> getCachedReward(TenantId tenantId, UUID rewardId);
    Mono<Void> cacheReward(TenantId tenantId, Reward reward, Duration ttl);
    Mono<Void> evictReward(TenantId tenantId, UUID rewardId);
}
