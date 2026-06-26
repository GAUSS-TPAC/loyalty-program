package com.yowyob.loyalty.domain.reward.service;

import com.yowyob.loyalty.domain.loyalty.model.points.PointsTransaction;
import com.yowyob.loyalty.domain.loyalty.port.out.PointsAccountRepository;
import com.yowyob.loyalty.domain.loyalty.port.out.PointsTransactionRepository;
import com.yowyob.loyalty.domain.reward.event.RewardGrantedEvent;
import com.yowyob.loyalty.domain.reward.exception.InsufficientPointsException;
import com.yowyob.loyalty.domain.reward.exception.RewardDomainException;
import com.yowyob.loyalty.domain.reward.model.GrantSource;
import com.yowyob.loyalty.domain.reward.model.RedemptionRequest;
import com.yowyob.loyalty.domain.reward.model.RedemptionResult;
import com.yowyob.loyalty.domain.reward.model.RewardGrant;
import com.yowyob.loyalty.domain.reward.port.in.RedeemRewardUseCase;
import com.yowyob.loyalty.domain.reward.port.out.RewardEventPublisherPort;
import com.yowyob.loyalty.domain.reward.port.out.RewardGrantRepository;
import com.yowyob.loyalty.domain.reward.port.out.RewardRepository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public class RedemptionService implements RedeemRewardUseCase {

    private final RewardRepository rewardRepo;
    private final RewardGrantRepository grantRepo;
    private final PointsAccountRepository pointsRepo;
    private final PointsTransactionRepository pointsTxRepo;
    private final RewardEventPublisherPort eventPublisher;

    public RedemptionService(RewardRepository rewardRepo, RewardGrantRepository grantRepo,
                              PointsAccountRepository pointsRepo, PointsTransactionRepository pointsTxRepo,
                              RewardEventPublisherPort eventPublisher) {
        this.rewardRepo = rewardRepo;
        this.grantRepo = grantRepo;
        this.pointsRepo = pointsRepo;
        this.pointsTxRepo = pointsTxRepo;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Mono<RedemptionResult> redeem(RedemptionRequest request) {
        return rewardRepo.findByIdAndTenant(request.rewardId(), request.tenantId())
                .flatMap(reward -> {
                    if (!reward.isAvailableAt(Instant.now()))
                        return Mono.error(new RewardDomainException("La récompense n'est pas disponible"));
                    if (!reward.isRedeemableWithPoints())
                        return Mono.error(new RewardDomainException("Cette récompense n'est pas échangeable contre des points"));

                    return Mono.fromCallable(() ->
                            pointsRepo.findByMemberId(request.tenantId(), request.memberId())
                                    .orElseThrow(() -> new InsufficientPointsException(reward.costInPoints(), 0)))
                            .flatMap(account -> {
                                if (!account.hasEnoughPoints(reward.costInPoints()))
                                    return Mono.error(new InsufficientPointsException(
                                            reward.costInPoints(), account.getAvailablePoints()));

                                reward.decrementStock();
                                var updatedAccount = account.spend(reward.costInPoints());
                                var tx = PointsTransaction.forDebit(account.getId(), request.tenantId(),
                                        reward.costInPoints(), updatedAccount.getAvailablePoints());

                                return rewardRepo.save(reward)
                                        .then(Mono.fromCallable(() -> {
                                            pointsRepo.save(updatedAccount);
                                            pointsTxRepo.save(tx);
                                            return updatedAccount;
                                        }))
                                        .flatMap(saved -> {
                                            RewardGrant grant = RewardGrant.create(UUID.randomUUID(),
                                                    request.tenantId(), request.memberId(),
                                                    reward, GrantSource.POINTS_REDEMPTION, null, null);
                                            return grantRepo.save(grant)
                                                    .map(RewardGrant::activate)
                                                    .flatMap(grantRepo::save)
                                                    .flatMap(activeGrant -> eventPublisher.publish(
                                                            new RewardGrantedEvent(UUID.randomUUID(), Instant.now(),
                                                                    request.tenantId(), request.memberId(),
                                                                    activeGrant.id(), activeGrant.rewardId(),
                                                                    activeGrant.rewardName(), activeGrant.rewardType(),
                                                                    GrantSource.POINTS_REDEMPTION))
                                                            .thenReturn(new RedemptionResult(
                                                                    activeGrant,
                                                                    reward.costInPoints(),
                                                                    saved.getAvailablePoints())));
                                        });
                            });
                });
    }
}
