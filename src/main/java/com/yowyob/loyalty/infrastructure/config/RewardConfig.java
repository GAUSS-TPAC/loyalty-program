package com.yowyob.loyalty.infrastructure.config;

import com.yowyob.loyalty.application.reward.handler.*;
import com.yowyob.loyalty.domain.loyalty.port.out.PointsAccountRepository;
import com.yowyob.loyalty.domain.loyalty.port.out.PointsTransactionRepository;
import com.yowyob.loyalty.domain.reward.model.Reward;
import com.yowyob.loyalty.domain.reward.model.RewardGrant;
import com.yowyob.loyalty.domain.reward.port.in.*;
import com.yowyob.loyalty.domain.reward.port.out.*;
import com.yowyob.loyalty.domain.reward.service.*;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.port.in.CreditWalletUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

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

    // Each *Handler below is @Service-scanned under its own concrete type AND is also the
    // implementation backing the *Service it wraps (which itself implements the same
    // interface) -- e.g. GrantRewardService and GrantRewardHandler both implement
    // GrantRewardUseCase. That leaves 2-3 beans matching a given interface type;
    // @Primary picks the intended Handler-backed bean unambiguously.
    //
    // GetRewardCatalogHandler additionally implements *two* interfaces at once, so simply
    // marking both explicit beans @Primary would leave two primaries competing for
    // GetRewardCatalogUseCase (and for GetMemberGrantsUseCase). Wrapping each in a
    // single-interface adapter avoids that cross-contamination.

    @Bean
    @Primary
    public CreateRewardUseCase createRewardUseCase(CreateRewardHandler handler) {
        return handler;
    }

    @Bean
    @Primary
    public UpdateRewardUseCase updateRewardUseCase(UpdateRewardHandler handler) {
        return handler;
    }

    @Bean
    @Primary
    public GetRewardCatalogUseCase getRewardCatalogUseCase(GetRewardCatalogHandler handler) {
        return new GetRewardCatalogUseCase() {
            @Override
            public Flux<Reward> getCatalog(TenantId tenantId, boolean activeOnly, int page, int size) {
                return handler.getCatalog(tenantId, activeOnly, page, size);
            }

            @Override
            public Mono<Reward> getReward(TenantId tenantId, UUID rewardId) {
                return handler.getReward(tenantId, rewardId);
            }
        };
    }

    @Bean
    @Primary
    public GetMemberGrantsUseCase getMemberGrantsUseCase(GetRewardCatalogHandler handler) {
        return new GetMemberGrantsUseCase() {
            @Override
            public Flux<RewardGrant> getActiveGrants(TenantId tenantId, UserId memberId) {
                return handler.getActiveGrants(tenantId, memberId);
            }

            @Override
            public Flux<RewardGrant> getAllGrants(TenantId tenantId, UserId memberId, int page, int size) {
                return handler.getAllGrants(tenantId, memberId, page, size);
            }

            @Override
            public Mono<RewardGrant> getGrant(TenantId tenantId, UUID grantId) {
                return handler.getGrant(tenantId, grantId);
            }
        };
    }

    @Bean
    @Primary
    public RedeemRewardUseCase redeemRewardUseCase(RedeemRewardHandler handler) {
        return handler;
    }

    @Bean
    @Primary
    public GrantRewardUseCase grantRewardUseCase(GrantRewardHandler handler) {
        return handler;
    }

    @Bean
    @Primary
    public ConsumeGrantUseCase consumeGrantUseCase(ConsumeGrantHandler handler) {
        return handler;
    }
}
