package com.yowyob.loyaulty.program.application.reward.handler;

import com.yowyob.loyaulty.program.domain.member.port.out.PointsAccountRepository;
import com.yowyob.loyaulty.program.domain.member.port.out.PointsTransactionRepository;
import com.yowyob.loyaulty.program.domain.reward.model.Reward;
import com.yowyob.loyaulty.program.domain.reward.model.RewardGrant;
import com.yowyob.loyaulty.program.domain.reward.model.enums.RewardType;
import com.yowyob.loyaulty.program.domain.reward.port.in.RedeemRewardUseCase;
import com.yowyob.loyaulty.program.domain.reward.port.out.RewardGrantRepository;
import com.yowyob.loyaulty.program.domain.reward.port.out.RewardRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.shared.exception.AppException;
import com.yowyob.loyaulty.program.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class RedemptionHandler implements RedeemRewardUseCase {

    private final RewardRepository rewardRepository;
    private final RewardGrantRepository grantRepository;
    private final PointsAccountRepository pointsAccountRepository;
    private final PointsTransactionRepository pointsTransactionRepository;

    public RedemptionHandler(RewardRepository rewardRepository,
                              RewardGrantRepository grantRepository,
                              PointsAccountRepository pointsAccountRepository,
                              PointsTransactionRepository pointsTransactionRepository) {
        this.rewardRepository = rewardRepository;
        this.grantRepository = grantRepository;
        this.pointsAccountRepository = pointsAccountRepository;
        this.pointsTransactionRepository = pointsTransactionRepository;
    }

    @Override
    public Mono<RewardGrant> redeem(TenantId tenantId, String memberId, UUID rewardId) {
        return rewardRepository.findById(rewardId, tenantId)
                .switchIfEmpty(Mono.error(new RewardNotFoundException(rewardId)))
                .flatMap(reward -> {
                    if (!reward.isAvailable()) {
                        return Mono.error(new RewardNotAvailableException(rewardId));
                    }
                    return pointsAccountRepository.findByMemberId(memberId, tenantId)
                            .switchIfEmpty(Mono.error(new InsufficientPointsException(0, reward.getCostPoints())))
                            .flatMap(account -> {
                                if (!account.canSpend(reward.getCostPoints())) {
                                    return Mono.error(new InsufficientPointsException(
                                            account.getAvailablePoints(), reward.getCostPoints()));
                                }

                                // Débit atomique : points + création du grant
                                var tx = account.spend(reward.getCostPoints(),
                                        "Rédemption : " + reward.getName(), rewardId.toString());
                                reward.decrementStock();

                                Instant expiresAt = reward.getValidUntil() != null
                                        ? reward.getValidUntil()
                                        : Instant.now().plusSeconds(365L * 24 * 3600);

                                RewardGrant grant = RewardGrant.create(
                                        tenantId, memberId, rewardId, expiresAt);

                                return Mono.zip(
                                        pointsTransactionRepository.save(tx),
                                        pointsAccountRepository.save(account),
                                        rewardRepository.save(reward),
                                        grantRepository.save(grant)
                                ).map(t -> t.getT4());
                            });
                });
    }

    @Override
    public Mono<RewardGrant> consume(TenantId tenantId, UUID grantId, String useContext) {
        return grantRepository.findById(grantId, tenantId)
                .switchIfEmpty(Mono.error(new GrantNotFoundException(grantId)))
                .flatMap(grant -> {
                    grant.consume(useContext);
                    return grantRepository.save(grant);
                });
    }

    @Override
    public Flux<RewardGrant> listActiveGrants(TenantId tenantId, String memberId) {
        return grantRepository.findActiveByMember(memberId, tenantId);
    }

    @Override
    public Flux<Reward> listCatalog(TenantId tenantId) {
        return rewardRepository.findActiveByTenant(tenantId);
    }

    @Override
    public Mono<Reward> createReward(TenantId tenantId, String name, String description,
                                      String type, long costPoints, Integer stock,
                                      String validFrom, String validUntil) {
        Reward reward = Reward.create(
                tenantId, name, description,
                RewardType.valueOf(type.toUpperCase()),
                costPoints, stock,
                validFrom != null ? Instant.parse(validFrom) : null,
                validUntil != null ? Instant.parse(validUntil) : null
        );
        return rewardRepository.save(reward);
    }

    // ── Exceptions internes ───────────────────────────────────────────────

    static class RewardNotFoundException extends AppException {
        RewardNotFoundException(UUID id) {
            super(ErrorCode.REWARD_NOT_FOUND, "Reward not found: " + id);
        }
    }

    static class RewardNotAvailableException extends AppException {
        RewardNotAvailableException(UUID id) {
            super(ErrorCode.REWARD_OUT_OF_STOCK, "Reward not available or out of stock: " + id);
        }
    }

    static class InsufficientPointsException extends AppException {
        InsufficientPointsException(long available, long required) {
            super(ErrorCode.INSUFFICIENT_BALANCE,
                    "Insufficient points: available=" + available + ", required=" + required);
        }
    }

    static class GrantNotFoundException extends AppException {
        GrantNotFoundException(UUID id) {
            super(ErrorCode.REWARD_GRANT_NOT_FOUND, "Reward grant not found: " + id);
        }
    }
}
