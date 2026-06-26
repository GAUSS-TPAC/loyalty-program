package com.yowyob.loyalty.application.reward.handler;

import com.yowyob.loyalty.domain.reward.model.Reward;
import com.yowyob.loyalty.domain.reward.model.RewardType;
import com.yowyob.loyalty.domain.reward.model.RewardValue;
import com.yowyob.loyalty.domain.reward.port.in.CreateRewardUseCase;
import com.yowyob.loyalty.domain.reward.service.RewardCatalogService;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@Service
public class CreateRewardHandler implements CreateRewardUseCase {

    private final RewardCatalogService catalogService;

    public CreateRewardHandler(RewardCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    public Mono<Reward> createReward(TenantId tenantId, String name, String description,
                                     RewardType type, RewardValue value, long costInPoints,
                                     Integer stockTotal, Instant validFrom, Instant validUntil,
                                     int grantExpiryDays, String imageUrl, Map<String, Object> metadata,
                                     String idempotencyKey) {
        return catalogService.createReward(tenantId, name, description, type, value, costInPoints,
                stockTotal, validFrom, validUntil, grantExpiryDays, imageUrl, metadata, idempotencyKey);
    }
}
