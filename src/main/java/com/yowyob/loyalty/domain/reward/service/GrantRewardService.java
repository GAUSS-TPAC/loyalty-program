package com.yowyob.loyalty.domain.reward.service;

import com.yowyob.loyalty.domain.reward.event.RewardGrantedEvent;
import com.yowyob.loyalty.domain.reward.exception.RewardDomainException;
import com.yowyob.loyalty.domain.reward.model.GrantSource;
import com.yowyob.loyalty.domain.reward.model.RewardGrant;
import com.yowyob.loyalty.domain.reward.port.in.GrantRewardUseCase;
import com.yowyob.loyalty.domain.reward.port.out.RewardEventPublisherPort;
import com.yowyob.loyalty.domain.reward.port.out.RewardGrantRepository;
import com.yowyob.loyalty.domain.reward.port.out.RewardRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public class GrantRewardService implements GrantRewardUseCase {

    private final RewardRepository rewardRepo;
    private final RewardGrantRepository grantRepo;
    private final RewardEventPublisherPort eventPublisher;

    public GrantRewardService(RewardRepository rewardRepo, RewardGrantRepository grantRepo,
                               RewardEventPublisherPort eventPublisher) {
        this.rewardRepo = rewardRepo;
        this.grantRepo = grantRepo;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Mono<RewardGrant> grantReward(TenantId tenantId, UserId memberId, UUID rewardId,
                                          GrantSource source, UUID sourceRuleId,
                                          String sourceEventKey, String idempotencyKey) {
        return rewardRepo.findByIdAndTenant(rewardId, tenantId)
                .flatMap(reward -> {
                    if (!reward.isAvailableAt(Instant.now()))
                        return Mono.error(new RewardDomainException("La récompense n'est pas disponible"));

                    reward.decrementStock();
                    return rewardRepo.save(reward)
                            .flatMap(savedReward -> {
                                RewardGrant grant = RewardGrant.create(UUID.randomUUID(), tenantId, memberId,
                                        savedReward, source, sourceRuleId, sourceEventKey);
                                return grantRepo.save(grant)
                                        .map(RewardGrant::activate)
                                        .flatMap(grantRepo::save)
                                        .flatMap(activeGrant -> eventPublisher.publish(
                                                new RewardGrantedEvent(UUID.randomUUID(), Instant.now(),
                                                        tenantId, memberId, activeGrant.id(),
                                                        activeGrant.rewardId(), activeGrant.rewardName(),
                                                        activeGrant.rewardType(), source))
                                                .thenReturn(activeGrant));
                            });
                });
    }
}
