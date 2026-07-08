package com.yowyob.loyalty.infrastructure.config;

import com.yowyob.loyalty.domain.loyalty.port.in.*;
import com.yowyob.loyalty.domain.loyalty.port.out.*;
import com.yowyob.loyalty.domain.loyalty.service.*;
import com.yowyob.loyalty.domain.loyalty.service.evaluator.*;
import com.yowyob.loyalty.domain.loyalty.service.executor.*;
import com.yowyob.loyalty.domain.loyalty.model.points.PointsAccount;
import com.yowyob.loyalty.domain.loyalty.model.points.PointsTransaction;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.port.in.CreditWalletUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;

import java.util.List;

@Configuration
public class LoyaltyConfig {

    @Bean
    public List<ConditionEvaluator> conditionEvaluators() {
        return List.of(
                new CumulativeCountEvaluator(),
                new CumulativeAmountEvaluator(),
                new PointsBalanceEvaluator(),
                new TierEvaluator(),
                new TimeWindowEvaluator(),
                new FirstEventEvaluator()
        );
    }

    @Bean
    public List<EffectExecutor> effectExecutors() {
        return List.of(
                new CreditPointsExecutor(),
                new CreditWalletExecutor(),
                new GrantRewardExecutor(),
                new ResetCounterExecutor(),
                new UpdateTierExecutor(),
                new SendNotificationExecutor()
        );
    }

    @Bean
    public RuleEngine ruleEngine(List<ConditionEvaluator> conditionEvaluators, List<EffectExecutor> effectExecutors) {
        return new RuleEngine(conditionEvaluators, effectExecutors);
    }

    @Bean
    public CounterService counterService() {
        return new CounterService();
    }

    @Bean
    public TierCalculationService tierCalculationService() {
        return new TierCalculationService();
    }

    @Bean
    public LoyaltyDomainService loyaltyDomainService(
            RuleEngine ruleEngine,
            CounterService counterService,
            TierCalculationService tierCalculationService,
            RuleRepository ruleRepo,
            PointsAccountRepository pointsRepo,
            PointsTransactionRepository pointsTxRepo,
            CounterRepository counterRepo,
            MemberTierRepository tierRepo,
            TierPolicyRepository tierPolicyRepo,
            RuleCachePort ruleCache,
            LoyaltyEventPublisherPort eventPublisher,
            @Qualifier("creditWalletHandler") CreditWalletUseCase creditWalletUseCase,
            @Nullable RewardGrantPort rewardGrantPort,
            @Nullable ActiveCampaignPort activeCampaignPort
    ) {
        return new LoyaltyDomainService(
                ruleEngine,
                counterService,
                tierCalculationService,
                ruleRepo,
                pointsRepo,
                pointsTxRepo,
                counterRepo,
                tierRepo,
                tierPolicyRepo,
                ruleCache,
                eventPublisher,
                creditWalletUseCase,
                rewardGrantPort,
                activeCampaignPort
        );
    }

    // LoyaltyDomainService implements all 5 use-case interfaces below directly, so
    // re-exposing it verbatim under each interface type makes every explicit bean also
    // match every *other* interface (and the raw loyaltyDomainService bean too), causing
    // NoUniqueBeanDefinitionException. Single-interface adapters here, plus @Primary,
    // keep exactly one unambiguous candidate per interface type.

    @Bean
    @Primary
    public ProcessEventUseCase processEventUseCase(LoyaltyDomainService loyaltyDomainService) {
        return loyaltyDomainService::processEvent;
    }

    @Bean
    @Primary
    public CreateRuleUseCase createRuleUseCase(LoyaltyDomainService loyaltyDomainService) {
        return loyaltyDomainService::createRule;
    }

    @Bean
    @Primary
    public ActivateRuleUseCase activateRuleUseCase(LoyaltyDomainService loyaltyDomainService) {
        return loyaltyDomainService::activateRule;
    }

    @Bean
    @Primary
    public GetMemberPointsUseCase getMemberPointsUseCase(LoyaltyDomainService loyaltyDomainService) {
        return new GetMemberPointsUseCase() {
            @Override
            public PointsAccount getPoints(TenantId tenantId, UserId memberId) {
                return loyaltyDomainService.getPoints(tenantId, memberId);
            }

            @Override
            public List<PointsTransaction> getPointsHistory(TenantId tenantId, UserId memberId, int page, int size) {
                return loyaltyDomainService.getPointsHistory(tenantId, memberId, page, size);
            }
        };
    }

    @Bean
    @Primary
    public GetMemberTierUseCase getMemberTierUseCase(LoyaltyDomainService loyaltyDomainService) {
        return loyaltyDomainService::getTier;
    }
}
