package com.yowyob.loyalty.application.reward.handler;

import com.yowyob.loyalty.domain.reward.model.Reward;
import com.yowyob.loyalty.domain.reward.port.in.UpdateRewardUseCase;
import com.yowyob.loyalty.domain.reward.service.RewardCatalogService;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
public class UpdateRewardHandler implements UpdateRewardUseCase {

    private final RewardCatalogService catalogService;

    public UpdateRewardHandler(RewardCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    public Mono<Reward> updateReward(TenantId tenantId, UUID rewardId, String name, String description,
                                     String imageUrl, Map<String, Object> metadata) {
        return catalogService.updateReward(tenantId, rewardId, name, description, imageUrl, metadata);
    }

    @Override
    public Mono<Reward> activateReward(TenantId tenantId, UUID rewardId) {
        return catalogService.activateReward(tenantId, rewardId);
    }

    @Override
    public Mono<Reward> pauseReward(TenantId tenantId, UUID rewardId) {
        return catalogService.pauseReward(tenantId, rewardId);
    }

    @Override
    public Mono<Reward> archiveReward(TenantId tenantId, UUID rewardId) {
        return catalogService.archiveReward(tenantId, rewardId);
    }
}
