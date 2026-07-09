package com.yowyob.loyalty.domain.loyalty.service;

import com.yowyob.loyalty.domain.loyalty.model.counter.Counter;
import com.yowyob.loyalty.domain.loyalty.model.engine.EvaluationContext;
import com.yowyob.loyalty.domain.loyalty.model.engine.RuleEvaluationResult;
import com.yowyob.loyalty.domain.loyalty.model.event.AppliedEffect;
import com.yowyob.loyalty.domain.loyalty.model.event.EventProcessingResult;
import com.yowyob.loyalty.domain.loyalty.model.event.IncomingEvent;
import com.yowyob.loyalty.domain.loyalty.model.points.PointsAccount;
import com.yowyob.loyalty.domain.loyalty.model.points.PointsTransaction;
import com.yowyob.loyalty.domain.loyalty.model.rule.Rule;
import com.yowyob.loyalty.domain.loyalty.model.tier.MemberTier;
import com.yowyob.loyalty.domain.loyalty.model.tier.TierLevel;
import com.yowyob.loyalty.domain.loyalty.model.tier.TierPolicy;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionType;
import com.yowyob.loyalty.domain.loyalty.model.rule.EffectDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.TriggerDefinition;
import com.yowyob.loyalty.domain.loyalty.exception.LoyaltyDomainException;
import com.yowyob.loyalty.domain.loyalty.port.in.*;
import com.yowyob.loyalty.domain.loyalty.port.out.*;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.time.Instant;
import com.yowyob.loyalty.domain.loyalty.port.out.ActiveCampaignPort;
import com.yowyob.loyalty.domain.loyalty.port.out.RewardGrantPort;
import com.yowyob.loyalty.domain.loyalty.service.executor.EffectExecutionContext;
import com.yowyob.loyalty.domain.wallet.port.in.CreditWalletUseCase;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LoyaltyDomainService implements ProcessEventUseCase, CreateRuleUseCase, ActivateRuleUseCase,
        GetMemberPointsUseCase, GetMemberTierUseCase {

    private final RuleEngine ruleEngine;
    private final CounterService counterService;
    private final TierCalculationService tierService;

    private final RuleRepository ruleRepo;
    private final PointsAccountRepository pointsRepo;
    private final PointsTransactionRepository pointsTxRepo;
    private final CounterRepository counterRepo;
    private final MemberTierRepository tierRepo;
    private final TierPolicyRepository tierPolicyRepo;
    private final RuleCachePort ruleCache;
    private final LoyaltyEventPublisherPort eventPublisher;
    private final CreditWalletUseCase creditWalletUseCase;
    private final RewardGrantPort rewardGrantPort;
    private final ActiveCampaignPort activeCampaignPort;

    public LoyaltyDomainService(RuleEngine ruleEngine,
                                CounterService counterService,
                                TierCalculationService tierService,
                                RuleRepository ruleRepo,
                                PointsAccountRepository pointsRepo,
                                PointsTransactionRepository pointsTxRepo,
                                CounterRepository counterRepo,
                                MemberTierRepository tierRepo,
                                TierPolicyRepository tierPolicyRepo,
                                RuleCachePort ruleCache,
                                LoyaltyEventPublisherPort eventPublisher,
                                CreditWalletUseCase creditWalletUseCase,
                                RewardGrantPort rewardGrantPort,
                                ActiveCampaignPort activeCampaignPort) {
        this.ruleEngine = ruleEngine;
        this.counterService = counterService;
        this.tierService = tierService;
        this.ruleRepo = ruleRepo;
        this.pointsRepo = pointsRepo;
        this.pointsTxRepo = pointsTxRepo;
        this.counterRepo = counterRepo;
        this.tierRepo = tierRepo;
        this.tierPolicyRepo = tierPolicyRepo;
        this.ruleCache = ruleCache;
        this.eventPublisher = eventPublisher;
        this.creditWalletUseCase = creditWalletUseCase;
        this.rewardGrantPort = rewardGrantPort;
        this.activeCampaignPort = activeCampaignPort;
    }

    @Override
    public EventProcessingResult processEvent(IncomingEvent event) {
        // 1. Check idempotency
        if (event.idempotencyKey() != null && pointsTxRepo.existsByEventIdempotencyKey(event.tenantId(), event.idempotencyKey())) {
            return new EventProcessingResult(UUID.randomUUID().toString(), event.tenantId(), event.memberId(), 
                    List.of(), List.of(), java.time.Instant.now()); // Already processed
        }

        // 2. Load Evaluation Context
        Optional<PointsAccount> existingAccount = pointsRepo.findByMemberId(event.tenantId(), event.memberId());
        PointsAccount pointsAccount = existingAccount
                .orElseGet(() -> PointsAccount.create(UUID.randomUUID(), event.tenantId(), event.memberId()));
        if (existingAccount.isEmpty()) {
            // Persist the brand-new account before any points_transactions row below can
            // reference its id (points_transactions.points_account_id has a FK to it).
            pointsAccount = pointsRepo.save(pointsAccount);
        }

        MemberTier memberTier = tierRepo.findByMemberId(event.tenantId(), event.memberId())
                .orElseGet(() -> MemberTier.defaultTier(UUID.randomUUID(), event.tenantId(), event.memberId()));

        Map<String, Counter> counters = counterRepo.findAllByMember(event.tenantId(), event.memberId())
                .stream().collect(Collectors.toMap(Counter::counterKey, Function.identity()));

        TierPolicy tierPolicy = tierPolicyRepo.findByTenantId(event.tenantId())
                .orElseGet(() -> TierPolicy.defaults(event.tenantId()));

        // 3. Load Active Rules (with cache)
        List<Rule> rules = ruleCache.getCachedRules(event.tenantId(), event.eventType());
        if (rules == null || rules.isEmpty()) {
            rules = ruleRepo.findActiveRulesByTenantAndEvent(event.tenantId(), event.eventType());
            ruleCache.cacheRules(event.tenantId(), event.eventType(), rules);
        }

        // 4. Progress stamp counters for matching triggers before rule evaluation
        incrementCountersForMatchedTriggers(event, rules, counters);

        EvaluationContext context = new EvaluationContext(event, pointsAccount, memberTier, counters, tierPolicy);

        // 5. Evaluate Rules
        EffectExecutionContext effectContext = new EffectExecutionContext();
        List<RuleEvaluationResult> ruleResults = ruleEngine.process(rules, context, effectContext);

        // 5. Apply State Changes based on Pending Operations
        List<AppliedEffect> finalEffects = ruleResults.stream()
                .flatMap(r -> r.appliedEffects().stream())
                .collect(Collectors.toList());

        PointsAccount updatedAccount = pointsAccount;
        Map<String, Counter> updatedCounters = new HashMap<>(counters);
        MemberTier updatedTier = memberTier;

        // Apply Points
        java.util.List<ActiveCampaignPort.CampaignBonus> activeBonuses =
                activeCampaignPort != null
                        ? activeCampaignPort.getActiveBonuses(event.tenantId(), event.eventType())
                        : java.util.List.of();

        for (EffectExecutionContext.PointsOperation op : effectContext.getPendingPointsOperations()) {
            if ("CREDIT".equals(op.type())) {
                updatedAccount = updatedAccount.earn(op.amount());
                PointsTransaction tx = PointsTransaction.forCredit(updatedAccount.getId(), event.tenantId(),
                        op.amount(), updatedAccount.getAvailablePoints(), op.ruleId(), event.idempotencyKey());
                pointsTxRepo.save(tx);

                for (ActiveCampaignPort.CampaignBonus bonus : activeBonuses) {
                    long extra = bonus.calculateExtraPoints(op.amount());
                    if (extra > 0) {
                        updatedAccount = updatedAccount.earn(extra);
                        PointsTransaction bonusTx = PointsTransaction.forCredit(updatedAccount.getId(), event.tenantId(),
                                extra, updatedAccount.getAvailablePoints(), null, null);
                        pointsTxRepo.save(bonusTx);
                        finalEffects.add(new AppliedEffect("CAMPAIGN_BONUS", bonus.campaignId().toString(),
                                bonus.campaignName(), Map.of("extra_points", extra)));
                    }
                }
            }
        }

        // Apply Counters Increment (implicitly for any conditions needing them, logic can be customized per domain needs)
        // Here we just apply explicit resets/increments from effects
        for (EffectExecutionContext.CounterOperation op : effectContext.getPendingCounterOperations()) {
            Counter current = updatedCounters.get(op.counterKey());
            if ("INCREMENT".equals(op.operationType())) {
                updatedCounters.put(op.counterKey(), counterService.processIncrement(current, event, op.delta(), "LIFETIME"));
            } else if ("RESET".equals(op.operationType())) {
                updatedCounters.put(op.counterKey(), counterService.processReset(current, event));
            }
        }

        // Apply Wallet
        for (EffectExecutionContext.WalletOperation op : effectContext.getPendingWalletOperations()) {
            if (creditWalletUseCase != null) {
                creditWalletUseCase.credit(
                        event.tenantId(),
                        op.memberId(),
                        op.amount(),
                        com.yowyob.loyalty.domain.wallet.model.TransactionSource.CASHBACK,
                        event.idempotencyKey() != null ? event.idempotencyKey() + "-wallet" : UUID.randomUUID().toString(),
                        event.idempotencyKey() != null ? event.idempotencyKey() + "-wallet" : UUID.randomUUID().toString()
                ).subscribe();
            }
        }

        // Apply Reward Grants (bonification partner)
        for (EffectExecutionContext.RewardOperation op : effectContext.getPendingRewardOperations()) {
            if (rewardGrantPort != null) {
                rewardGrantPort.grantReward(event.tenantId(), op.memberId(), op.rewardId(), op.amount());
            }
        }

        // Apply Tier updates
        for (EffectExecutionContext.TierOperation op : effectContext.getPendingTierOperations()) {
            java.math.BigDecimal multiplier = tierService.getMultiplierForTier(op.newLevel(), tierPolicy);
            updatedTier = updatedTier.withLevel(op.newLevel(), multiplier);
        }

        // Re-evaluate Tier if points changed (optional: depending on business logic, here we do it automatically if policy is LIFETIME_POINTS)
        if (updatedAccount.getLifetimeEarned() != pointsAccount.getLifetimeEarned()) {
            Optional<TierLevel> autoNewTier = tierService.evaluateNewTier(updatedAccount, updatedTier, tierPolicy);
            if (autoNewTier.isPresent()) {
                java.math.BigDecimal autoMultiplier = tierService.getMultiplierForTier(autoNewTier.get(), tierPolicy);
                updatedTier = updatedTier.withLevel(autoNewTier.get(), autoMultiplier);
                
                finalEffects.add(new AppliedEffect("AUTO_UPDATE_TIER", null, "System", 
                        Map.of("new_tier", autoNewTier.get().name())));
            }
        }

        // 6. Persist state
        if (updatedAccount.getVersion() > pointsAccount.getVersion() || !updatedAccount.getId().equals(pointsAccount.getId())) {
            pointsRepo.save(updatedAccount);
        }

        for (Counter c : updatedCounters.values()) {
            counterRepo.save(c);
        }

        if (!updatedTier.equals(memberTier)) {
            tierRepo.save(updatedTier);
        }

        // 8. Publish Result
        List<String> notifications = effectContext.getPendingNotifications().stream()
                .map(EffectExecutionContext.NotificationOperation::template)
                .collect(Collectors.toList());

        EventProcessingResult finalResult = new EventProcessingResult(
                UUID.randomUUID().toString(),
                event.tenantId(),
                event.memberId(),
                finalEffects,
                notifications,
                java.time.Instant.now()
        );

        eventPublisher.publishProcessedEvent(finalResult);

        return finalResult;
    }

    @Override
    public Rule createRule(TenantId tenantId, String name, String description, TriggerDefinition trigger,
                           List<ConditionDefinition> conditions, List<EffectDefinition> effects, int priority,
                           Instant validFrom, Instant validUntil) {
        Rule rule = Rule.create(UUID.randomUUID(), tenantId, name, description, trigger, conditions, effects,
                priority, validFrom, validUntil);
        return ruleRepo.save(rule);
    }

    @Override
    public Rule activateRule(TenantId tenantId, UUID ruleId) {
        Rule rule = ruleRepo.findById(ruleId)
                .filter(r -> r.getTenantId().equals(tenantId))
                .orElseThrow(() -> new LoyaltyDomainException("Règle introuvable: " + ruleId));
        Rule activated = rule.activate();
        ruleCache.invalidateCache(tenantId);
        return ruleRepo.save(activated);
    }

    @Override
    public PointsAccount getPoints(TenantId tenantId, UserId memberId) {
        return pointsRepo.findByMemberId(tenantId, memberId)
                .orElseGet(() -> PointsAccount.create(UUID.randomUUID(), tenantId, memberId));
    }

    @Override
    public List<PointsTransaction> getPointsHistory(TenantId tenantId, UserId memberId, int page, int size) {
        return pointsRepo.findByMemberId(tenantId, memberId)
                .map(account -> pointsTxRepo.findByAccountId(account.getId(), size, page * size))
                .orElse(List.of());
    }

    @Override
    public MemberTier getTier(TenantId tenantId, UserId memberId) {
        return tierRepo.findByMemberId(tenantId, memberId)
                .orElseGet(() -> MemberTier.defaultTier(UUID.randomUUID(), tenantId, memberId));
    }

    private void incrementCountersForMatchedTriggers(IncomingEvent event, List<Rule> rules, Map<String, Counter> counters) {
        for (Rule rule : rules) {
            if (!rule.isActiveAt(event.occurredAt()) || !rule.triggerMatches(event)) {
                continue;
            }
            String counterKey = resolveCounterKey(rule);
            String windowType = resolveWindowType(rule);
            Counter current = counters.get(counterKey);
            Counter updated = counterService.processIncrement(current, event, 1, windowType);
            updated = new Counter(
                    updated.id(),
                    updated.tenantId(),
                    updated.memberId(),
                    counterKey,
                    updated.value(),
                    updated.windowType(),
                    updated.windowStart(),
                    updated.updatedAt()
            );
            counters.put(counterKey, updated);
        }
    }

    private static String resolveCounterKey(Rule rule) {
        return rule.getConditions().stream()
                .filter(c -> c.type() == ConditionType.CUMULATIVE_COUNT || c.type() == ConditionType.CUMULATIVE_AMOUNT)
                .map(ConditionDefinition::counterKey)
                .filter(key -> key != null && !key.isBlank())
                .findFirst()
                .orElse("rule_" + rule.getId() + "_count");
    }

    private static String resolveWindowType(Rule rule) {
        return rule.getConditions().stream()
                .map(ConditionDefinition::windowType)
                .filter(w -> w != null && !w.isBlank())
                .findFirst()
                .orElse("LIFETIME");
    }
}
