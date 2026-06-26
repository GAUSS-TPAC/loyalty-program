package com.yowyob.loyalty.domain.reward.service;

import com.yowyob.loyalty.domain.reward.event.RewardCreatedEvent;
import com.yowyob.loyalty.domain.reward.model.Reward;
import com.yowyob.loyalty.domain.reward.model.RewardType;
import com.yowyob.loyalty.domain.reward.model.RewardValue;
import com.yowyob.loyalty.domain.reward.port.in.CreateRewardUseCase;
import com.yowyob.loyalty.domain.reward.port.in.GetRewardCatalogUseCase;
import com.yowyob.loyalty.domain.reward.port.in.UpdateRewardUseCase;
import com.yowyob.loyalty.domain.reward.port.out.RewardCachePort;
import com.yowyob.loyalty.domain.reward.port.out.RewardEventPublisherPort;
import com.yowyob.loyalty.domain.reward.port.out.RewardRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class RewardCatalogService implements CreateRewardUseCase, UpdateRewardUseCase, GetRewardCatalogUseCase {

    private final RewardRepository rewardRepo;
    private final RewardEventPublisherPort eventPublisher;
    private final RewardCachePort rewardCache;

    public RewardCatalogService(RewardRepository rewardRepo, RewardEventPublisherPort eventPublisher, RewardCachePort rewardCache) {
        this.rewardRepo = rewardRepo;
        this.eventPublisher = eventPublisher;
        this.rewardCache = rewardCache;
    }

    @Override
    public Mono<Reward> createReward(TenantId tenantId, String name, String description,
                                     RewardType type, RewardValue value, long costInPoints,
                                     Integer stockTotal, Instant validFrom, Instant validUntil,
                                     int grantExpiryDays, String imageUrl, Map<String, Object> metadata,
                                     String idempotencyKey) {
        UUID id = UUID.randomUUID();
        Reward reward = Reward.create(id, tenantId, name, description, type, value, costInPoints,
                stockTotal, validFrom, validUntil, grantExpiryDays, imageUrl, metadata);
        return rewardRepo.save(reward)
                .flatMap(saved -> eventPublisher.publish(
                        new RewardCreatedEvent(UUID.randomUUID(), Instant.now(), tenantId, saved.id(), saved.name()))
                        .thenReturn(saved));
    }

    @Override
    public Mono<Reward> updateReward(TenantId tenantId, UUID rewardId, String name, String description,
                                     String imageUrl, Map<String, Object> metadata) {
        return rewardRepo.findByIdAndTenant(rewardId, tenantId)
                .map(reward -> reward.update(name, description, imageUrl, metadata))
                .flatMap(rewardRepo::save);
    }

    @Override
    public Mono<Reward> activateReward(TenantId tenantId, UUID rewardId) {
        return rewardRepo.findByIdAndTenant(rewardId, tenantId)
                .map(Reward::activate)
                .flatMap(rewardRepo::save)
                .flatMap(saved -> rewardCache.evictCatalog(tenantId).thenReturn(saved));
    }

    @Override
    public Mono<Reward> pauseReward(TenantId tenantId, UUID rewardId) {
        return rewardRepo.findByIdAndTenant(rewardId, tenantId)
                .map(Reward::pause)
                .flatMap(rewardRepo::save)
                .flatMap(saved -> rewardCache.evictCatalog(tenantId).thenReturn(saved));
    }

    @Override
    public Mono<Reward> archiveReward(TenantId tenantId, UUID rewardId) {
        return rewardRepo.findByIdAndTenant(rewardId, tenantId)
                .map(Reward::archive)
                .flatMap(rewardRepo::save)
                .flatMap(saved -> rewardCache.evictCatalog(tenantId).thenReturn(saved));
    }

    @Override
    public Flux<Reward> getCatalog(TenantId tenantId, boolean activeOnly, int page, int size) {
        return rewardRepo.findByTenant(tenantId, activeOnly, page, size);
    }

    @Override
    public Mono<Reward> getReward(TenantId tenantId, UUID rewardId) {
        return rewardRepo.findByIdAndTenant(rewardId, tenantId);
    }
}
