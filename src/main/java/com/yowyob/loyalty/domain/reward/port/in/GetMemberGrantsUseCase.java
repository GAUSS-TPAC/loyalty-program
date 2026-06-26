package com.yowyob.loyalty.domain.reward.port.in;

import com.yowyob.loyalty.domain.reward.model.RewardGrant;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetMemberGrantsUseCase {
    Flux<RewardGrant> getActiveGrants(TenantId tenantId, UserId memberId);
    Flux<RewardGrant> getAllGrants(TenantId tenantId, UserId memberId, int page, int size);
    Mono<RewardGrant> getGrant(TenantId tenantId, UUID grantId);
}
