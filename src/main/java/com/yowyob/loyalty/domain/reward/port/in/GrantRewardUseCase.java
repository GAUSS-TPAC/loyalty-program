package com.yowyob.loyalty.domain.reward.port.in;

import com.yowyob.loyalty.domain.reward.model.GrantSource;
import com.yowyob.loyalty.domain.reward.model.RewardGrant;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GrantRewardUseCase {
    Mono<RewardGrant> grantReward(
            TenantId tenantId,
            UserId memberId,
            UUID rewardId,
            GrantSource source,
            UUID sourceRuleId,
            String sourceEventKey,
            String idempotencyKey
    );
}
