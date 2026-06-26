package com.yowyob.loyalty.application.reward.handler;

import com.yowyob.loyalty.domain.reward.model.Reward;
import com.yowyob.loyalty.domain.reward.model.RewardGrant;
import com.yowyob.loyalty.domain.reward.port.in.GetMemberGrantsUseCase;
import com.yowyob.loyalty.domain.reward.port.in.GetRewardCatalogUseCase;
import com.yowyob.loyalty.domain.reward.port.out.RewardGrantRepository;
import com.yowyob.loyalty.domain.reward.service.RewardCatalogService;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class GetRewardCatalogHandler implements GetRewardCatalogUseCase, GetMemberGrantsUseCase {

    private final RewardCatalogService catalogService;
    private final RewardGrantRepository grantRepo;

    public GetRewardCatalogHandler(RewardCatalogService catalogService, RewardGrantRepository grantRepo) {
        this.catalogService = catalogService;
        this.grantRepo = grantRepo;
    }

    @Override
    public Flux<Reward> getCatalog(TenantId tenantId, boolean activeOnly, int page, int size) {
        return catalogService.getCatalog(tenantId, activeOnly, page, size);
    }

    @Override
    public Mono<Reward> getReward(TenantId tenantId, UUID rewardId) {
        return catalogService.getReward(tenantId, rewardId);
    }

    @Override
    public Flux<RewardGrant> getActiveGrants(TenantId tenantId, UserId memberId) {
        return grantRepo.findActiveByMember(memberId, tenantId);
    }

    @Override
    public Flux<RewardGrant> getAllGrants(TenantId tenantId, UserId memberId, int page, int size) {
        return grantRepo.findAllByMember(memberId, tenantId, page, size);
    }

    @Override
    public Mono<RewardGrant> getGrant(TenantId tenantId, UUID grantId) {
        return grantRepo.findByIdAndTenant(grantId, tenantId);
    }
}
