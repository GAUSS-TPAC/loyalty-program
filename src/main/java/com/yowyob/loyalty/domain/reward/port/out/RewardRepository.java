package com.yowyob.loyalty.domain.reward.port.out;

import com.yowyob.loyalty.domain.reward.model.Reward;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RewardRepository {
    Mono<Reward> save(Reward reward);
    Mono<Reward> findById(UUID id);
    Mono<Reward> findByIdAndTenant(UUID id, TenantId tenantId);
    Flux<Reward> findByTenant(TenantId tenantId, boolean activeOnly, int page, int size);
    Mono<Boolean> existsByIdAndTenant(UUID id, TenantId tenantId);
}
