package com.yowyob.loyalty.infrastructure.config;

import com.yowyob.loyalty.domain.loyalty.port.in.*;
import com.yowyob.loyalty.domain.loyalty.port.out.*;
import com.yowyob.loyalty.domain.loyalty.service.*;
import com.yowyob.loyalty.domain.loyalty.service.evaluator.*;
import com.yowyob.loyalty.domain.loyalty.service.executor.*;
import com.yowyob.loyalty.domain.wallet.port.in.CreditWalletUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
            @Nullable RewardGrantPort rewardGrantPort
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
                rewardGrantPort
        );
    }

    @Bean
    public ProcessEventUseCase processEventUseCase(LoyaltyDomainService loyaltyDomainService) {
        return loyaltyDomainService;
    }

    @Bean
    public CreateRuleUseCase createRuleUseCase(LoyaltyDomainService loyaltyDomainService) {
        return loyaltyDomainService;
    }

    @Bean
    public ActivateRuleUseCase activateRuleUseCase(LoyaltyDomainService loyaltyDomainService) {
        return loyaltyDomainService;
    }

    @Bean
    public GetMemberPointsUseCase getMemberPointsUseCase(LoyaltyDomainService loyaltyDomainService) {
        return loyaltyDomainService;
    }

    @Bean
    public GetMemberTierUseCase getMemberTierUseCase(LoyaltyDomainService loyaltyDomainService) {
        return loyaltyDomainService;
    }
}
