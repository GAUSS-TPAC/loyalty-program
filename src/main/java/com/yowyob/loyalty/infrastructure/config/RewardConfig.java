package com.yowyob.loyalty.infrastructure.config;

import com.yowyob.loyalty.application.reward.handler.*;
import com.yowyob.loyalty.domain.loyalty.port.out.PointsAccountRepository;
import com.yowyob.loyalty.domain.loyalty.port.out.PointsTransactionRepository;
import com.yowyob.loyalty.domain.reward.port.in.*;
import com.yowyob.loyalty.domain.reward.port.out.*;
import com.yowyob.loyalty.domain.reward.service.*;
import com.yowyob.loyalty.domain.wallet.port.in.CreditWalletUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RewardConfig {

    @Bean
    public RewardCatalogService rewardCatalogService(
            RewardRepository rewardRepo,
            RewardEventPublisherPort eventPublisher,
            RewardCachePort rewardCache) {
        return new RewardCatalogService(rewardRepo, eventPublisher, rewardCache);
    }

    @Bean
    public GrantRewardService grantRewardService(
            RewardRepository rewardRepo,
            RewardGrantRepository grantRepo,
            RewardEventPublisherPort eventPublisher) {
        return new GrantRewardService(rewardRepo, grantRepo, eventPublisher);
    }

    @Bean
    public RedemptionService redemptionService(
            RewardRepository rewardRepo,
            RewardGrantRepository grantRepo,
            PointsAccountRepository pointsRepo,
            PointsTransactionRepository pointsTxRepo,
            RewardEventPublisherPort eventPublisher) {
        return new RedemptionService(rewardRepo, grantRepo, pointsRepo, pointsTxRepo, eventPublisher);
    }

    @Bean
    public ConsumeGrantService consumeGrantService(
            RewardGrantRepository grantRepo,
            RewardEventPublisherPort eventPublisher,
            @Qualifier("creditWalletHandler") CreditWalletUseCase creditWalletUseCase) {
        return new ConsumeGrantService(grantRepo, eventPublisher, creditWalletUseCase);
    }

    @Bean
    public GrantExpiryService grantExpiryService(
            RewardGrantRepository grantRepo,
            RewardEventPublisherPort eventPublisher) {
        return new GrantExpiryService(grantRepo, eventPublisher);
    }

    @Bean
    public CreateRewardUseCase createRewardUseCase(CreateRewardHandler handler) {
        return handler;
    }

    @Bean
    public UpdateRewardUseCase updateRewardUseCase(UpdateRewardHandler handler) {
        return handler;
    }

    @Bean
    public GetRewardCatalogUseCase getRewardCatalogUseCase(GetRewardCatalogHandler handler) {
        return handler;
    }

    @Bean
    public GetMemberGrantsUseCase getMemberGrantsUseCase(GetRewardCatalogHandler handler) {
        return handler;
    }

    @Bean
    public RedeemRewardUseCase redeemRewardUseCase(RedeemRewardHandler handler) {
        return handler;
    }

    @Bean
    public GrantRewardUseCase grantRewardUseCase(GrantRewardHandler handler) {
        return handler;
    }

    @Bean
    public ConsumeGrantUseCase consumeGrantUseCase(ConsumeGrantHandler handler) {
        return handler;
    }
}
