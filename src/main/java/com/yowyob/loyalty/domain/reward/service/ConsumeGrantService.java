package com.yowyob.loyalty.domain.reward.service;

import com.yowyob.loyalty.domain.reward.event.RewardConsumedEvent;
import com.yowyob.loyalty.domain.reward.exception.GrantAlreadyUsedException;
import com.yowyob.loyalty.domain.reward.exception.GrantExpiredException;
import com.yowyob.loyalty.domain.reward.exception.GrantNotFoundException;
import com.yowyob.loyalty.domain.reward.exception.RewardDomainException;
import com.yowyob.loyalty.domain.reward.model.ConsumeGrantRequest;
import com.yowyob.loyalty.domain.reward.model.ConsumeGrantResult;
import com.yowyob.loyalty.domain.reward.model.RewardType;
import com.yowyob.loyalty.domain.reward.port.in.ConsumeGrantUseCase;
import com.yowyob.loyalty.domain.reward.port.out.RewardEventPublisherPort;
import com.yowyob.loyalty.domain.reward.port.out.RewardGrantRepository;
import com.yowyob.loyalty.domain.wallet.model.TransactionSource;
import com.yowyob.loyalty.domain.wallet.port.in.CreditWalletUseCase;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ConsumeGrantService implements ConsumeGrantUseCase {

    private final RewardGrantRepository grantRepo;
    private final RewardEventPublisherPort eventPublisher;
    private final CreditWalletUseCase creditWalletUseCase;

    public ConsumeGrantService(RewardGrantRepository grantRepo, RewardEventPublisherPort eventPublisher,
                                CreditWalletUseCase creditWalletUseCase) {
        this.grantRepo = grantRepo;
        this.eventPublisher = eventPublisher;
        this.creditWalletUseCase = creditWalletUseCase;
    }

    @Override
    public Mono<ConsumeGrantResult> consumeGrant(ConsumeGrantRequest request) {
        return grantRepo.findByIdAndTenant(request.grantId(), request.tenantId())
                .switchIfEmpty(Mono.error(new GrantNotFoundException(request.grantId())))
                .flatMap(grant -> {
                    if (!grant.memberId().equals(request.memberId()))
                        return Mono.error(new RewardDomainException("Grant n'appartient pas à ce membre"));

                    if (grant.isExpired()) {
                        grant.expire();
                        return grantRepo.save(grant)
                                .then(Mono.error(new GrantExpiredException(grant.id(), grant.expiresAt())));
                    }

                    if (!grant.status().isUsable()) {
                        if (grant.status().isFinal()) {
                            return Mono.error(new GrantAlreadyUsedException(grant.id()));
                        }
                        return Mono.error(new RewardDomainException("Grant non utilisable en état " + grant.status()));
                    }

                    String context = String.format("{\"order_reference\":\"%s\",\"order_amount\":%s,\"consumed_at\":\"%s\"}",
                            request.orderReference(), request.orderAmount(), Instant.now());

                    grant.consume(context);

                    BigDecimal discount = grant.rewardValue().calculateDiscount(request.orderAmount());
                    if (discount.compareTo(request.orderAmount()) > 0) {
                        discount = request.orderAmount();
                    }
                    BigDecimal finalAmount = request.orderAmount().subtract(discount);
                    boolean fullyConsumed = grant.status() == com.yowyob.loyalty.domain.reward.model.GrantStatus.USED;

                    final BigDecimal finalDiscount = discount;
                    final BigDecimal finalOrderAmount = finalAmount;

                    Mono<Void> cashbackMono = Mono.empty();
                    if (grant.rewardType() == RewardType.CASHBACK_WALLET) {
                        cashbackMono = creditWalletUseCase.credit(
                                request.tenantId(), request.memberId(),
                                grant.rewardValue().numericValue(),
                                TransactionSource.LOYALTY_REWARD,
                                grant.id().toString(),
                                "cashback-" + grant.id()
                        ).then();
                    }

                    return cashbackMono
                            .then(grantRepo.save(grant))
                            .flatMap(savedGrant -> eventPublisher.publish(
                                    new RewardConsumedEvent(UUID.randomUUID(), Instant.now(),
                                            request.tenantId(), request.memberId(),
                                            savedGrant.id(), savedGrant.rewardId(),
                                            request.orderReference(), finalDiscount))
                                    .thenReturn(new ConsumeGrantResult(savedGrant, finalDiscount, finalOrderAmount, fullyConsumed)));
                });
    }
}
