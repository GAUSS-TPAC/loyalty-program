package com.yowyob.loyalty.application.reward.handler;

import com.yowyob.loyalty.domain.reward.model.GrantSource;
import com.yowyob.loyalty.domain.reward.model.RewardGrant;
import com.yowyob.loyalty.domain.reward.port.in.GrantRewardUseCase;
import com.yowyob.loyalty.domain.reward.service.GrantRewardService;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class GrantRewardHandler implements GrantRewardUseCase {

    private final GrantRewardService grantService;

    public GrantRewardHandler(GrantRewardService grantService) {
        this.grantService = grantService;
    }

    @Override
    @Transactional
    public Mono<RewardGrant> grantReward(TenantId tenantId, UserId memberId, UUID rewardId,
                                          GrantSource source, UUID sourceRuleId,
                                          String sourceEventKey, String idempotencyKey) {
        return grantService.grantReward(tenantId, memberId, rewardId, source, sourceRuleId, sourceEventKey, idempotencyKey);
    }
}
