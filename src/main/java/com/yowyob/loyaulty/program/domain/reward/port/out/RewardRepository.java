package com.yowyob.loyaulty.program.domain.reward.port.out;

import com.yowyob.loyaulty.program.domain.reward.model.Reward;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RewardRepository {

    Mono<Reward> save(Reward reward);

    Mono<Reward> findById(UUID id, TenantId tenantId);

    Flux<Reward> findAllByTenant(TenantId tenantId);

    Flux<Reward> findActiveByTenant(TenantId tenantId);
}
