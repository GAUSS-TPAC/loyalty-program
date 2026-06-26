package com.yowyob.loyalty.domain.reward.port.out;

import com.yowyob.loyalty.domain.reward.model.RewardGrant;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface RewardGrantRepository {
    Mono<RewardGrant> save(RewardGrant grant);
    Mono<RewardGrant> findById(UUID id);
    Mono<RewardGrant> findByIdAndTenant(UUID id, TenantId tenantId);
    Mono<RewardGrant> findByIdempotencyKey(String key);
    Flux<RewardGrant> findActiveByMember(UserId memberId, TenantId tenantId);
    Flux<RewardGrant> findAllByMember(UserId memberId, TenantId tenantId, int page, int size);
    Flux<RewardGrant> findExpiredActive(Instant before);
}
