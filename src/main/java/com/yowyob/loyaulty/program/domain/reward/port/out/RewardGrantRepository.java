package com.yowyob.loyaulty.program.domain.reward.port.out;

import com.yowyob.loyaulty.program.domain.reward.model.RewardGrant;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RewardGrantRepository {

    Mono<RewardGrant> save(RewardGrant grant);

    Mono<RewardGrant> findById(UUID id, TenantId tenantId);

    Flux<RewardGrant> findActiveByMember(String memberId, TenantId tenantId);

    Flux<RewardGrant> findAllByMember(String memberId, TenantId tenantId);
}
