package com.yowyob.loyalty.domain.loyalty;

import com.yowyob.loyalty.domain.loyalty.model.counter.Counter;
import com.yowyob.loyalty.domain.loyalty.model.event.EventProcessingResult;
import com.yowyob.loyalty.domain.loyalty.model.event.IncomingEvent;
import com.yowyob.loyalty.domain.loyalty.model.points.ApiKeyPointsFlow;
import com.yowyob.loyalty.domain.loyalty.model.points.PointsAccount;
import com.yowyob.loyalty.domain.loyalty.model.points.PointsTransaction;
import com.yowyob.loyalty.domain.loyalty.model.rule.* ;
import com.yowyob.loyalty.domain.loyalty.model.tier.MemberTier;
import com.yowyob.loyalty.domain.loyalty.model.tier.TierPolicy;
import com.yowyob.loyalty.domain.loyalty.port.out.*;
import com.yowyob.loyalty.domain.loyalty.service.CounterService;
import com.yowyob.loyalty.domain.loyalty.service.LoyaltyDomainService;
import com.yowyob.loyalty.domain.loyalty.service.RuleEngine;
import com.yowyob.loyalty.domain.loyalty.service.TierCalculationService;
import com.yowyob.loyalty.domain.loyalty.service.evaluator.CumulativeCountEvaluator;
import com.yowyob.loyalty.domain.loyalty.service.executor.CreditPointsExecutor;
import com.yowyob.loyalty.domain.loyalty.service.executor.ResetCounterExecutor;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LoyaltyDomainServiceTest {

    private LoyaltyDomainService service;

    // Fakes
    private InMemoryRuleRepository ruleRepo = new InMemoryRuleRepository();
    private InMemoryPointsAccountRepository pointsRepo = new InMemoryPointsAccountRepository();
    private InMemoryCounterRepository counterRepo = new InMemoryCounterRepository();
    private InMemoryEventPublisher eventPublisher = new InMemoryEventPublisher();

    @BeforeEach
    void setUp() {
        RuleEngine engine = new RuleEngine(List.of(new CumulativeCountEvaluator()), List.of(new CreditPointsExecutor(), new ResetCounterExecutor()));
        service = new LoyaltyDomainService(engine, new CounterService(), new TierCalculationService(),
                ruleRepo, pointsRepo, new InMemoryPointsTransactionRepository(), counterRepo,
                new InMemoryMemberTierRepository(), new InMemoryTierPolicyRepository(),
                new InMemoryRuleCache(), eventPublisher, null, null, null);
    }

    @Test
    void processEvent_creditsPointsOnThirdPurchase() {
        TenantId t1 = new TenantId(UUID.randomUUID());
        UserId u1 = new UserId(UUID.randomUUID());

        Rule rule = Rule.create(UUID.randomUUID(), t1, "Bonus", "Desc",
                new TriggerDefinition("purchase", null),
                List.of(new ConditionDefinition(ConditionType.CUMULATIVE_COUNT, ConditionOperator.GREATER_THAN_OR_EQUAL, 3, "LIFETIME", "purchases")),
                List.of(new EffectDefinition(EffectType.CREDIT_POINTS, Map.of("amount", 100))),
                10, null, null).activate();
        ruleRepo.rules.add(rule);

        EventProcessingResult result = null;
        for (int i = 1; i <= 3; i++) {
            result = service.processEvent(new IncomingEvent("purchase", t1, u1, "evt-" + i, Instant.now(), Map.of()));
        }

        assertNotNull(result);
        assertTrue(result.hasEffects());
        assertEquals("CREDIT_POINTS", result.effectsApplied().get(0).effectType());

        PointsAccount account = pointsRepo.findByMemberId(t1, u1).orElseThrow();
        assertEquals(100, account.getAvailablePoints());
    }

    @Test
    void processEvent_idempotentByTransactionKey_doesNotDoubleCredit() {
        TenantId t1 = new TenantId(UUID.randomUUID());
        UserId u1 = new UserId(UUID.randomUUID());
        Rule rule = Rule.create(UUID.randomUUID(), t1, "Bonus", "Desc",
                new TriggerDefinition("purchase", null),
                List.of(new ConditionDefinition(ConditionType.CUMULATIVE_COUNT, ConditionOperator.GREATER_THAN_OR_EQUAL, 1, "LIFETIME", "purchases")),
                List.of(new EffectDefinition(EffectType.CREDIT_POINTS, Map.of("amount", 50))),
                10, null, null).activate();
        ruleRepo.rules.add(rule);

        IncomingEvent event = new IncomingEvent("purchase", t1, u1, "same-key", Instant.now(), Map.of());
        service.processEvent(event);
        service.processEvent(event);

        PointsAccount account = pointsRepo.findByMemberId(t1, u1).orElseThrow();
        assertEquals(50, account.getAvailablePoints());
    }

    @Test
    void processEvent_incrementsCounterWithoutCreditingWhenBelowThreshold() {
        TenantId t1 = new TenantId(UUID.randomUUID());
        UserId u1 = new UserId(UUID.randomUUID());
        Rule rule = Rule.create(UUID.randomUUID(), t1, "Stamp", "Desc",
                new TriggerDefinition("purchase", null),
                List.of(new ConditionDefinition(ConditionType.CUMULATIVE_COUNT, ConditionOperator.GREATER_THAN_OR_EQUAL, 10, "LIFETIME", "purchases")),
                List.of(new EffectDefinition(EffectType.CREDIT_POINTS, Map.of("amount", 100))),
                10, null, null).activate();
        ruleRepo.rules.add(rule);

        service.processEvent(new IncomingEvent("purchase", t1, u1, "e1", Instant.now(), Map.of()));
        EventProcessingResult second = service.processEvent(new IncomingEvent("purchase", t1, u1, "e2", Instant.now(), Map.of()));

        assertFalse(second.hasEffects());
        Counter counter = counterRepo.findByKey(t1, u1, "purchases").orElseThrow();
        assertEquals(2, counter.value());
        assertTrue(pointsRepo.findByMemberId(t1, u1).map(a -> a.getAvailablePoints() == 0).orElse(true));
    }

    @Test
    void processEvent_tierMultiplier_doublesPoints() {
        TenantId t1 = new TenantId(UUID.randomUUID());
        UserId u1 = new UserId(UUID.randomUUID());
        Rule rule = Rule.create(UUID.randomUUID(), t1, "Gold bonus", "Desc",
                new TriggerDefinition("purchase", null),
                List.of(new ConditionDefinition(ConditionType.CUMULATIVE_COUNT, ConditionOperator.GREATER_THAN_OR_EQUAL, 1, "LIFETIME", "purchases")),
                List.of(new EffectDefinition(EffectType.CREDIT_POINTS, Map.of("amount", 500))),
                10, null, null).activate();
        ruleRepo.rules.add(rule);

        InMemoryMemberTierRepository tierRepository = new InMemoryMemberTierRepository();
        tierRepository.tier = MemberTier.defaultTier(UUID.randomUUID(), t1, u1)
                .withLevel(com.yowyob.loyalty.domain.loyalty.model.tier.TierLevel.GOLD, new java.math.BigDecimal("2.0"));
        LoyaltyDomainService goldService = new LoyaltyDomainService(
                new RuleEngine(List.of(new CumulativeCountEvaluator()), List.of(new CreditPointsExecutor())),
                new CounterService(), new TierCalculationService(),
                ruleRepo, pointsRepo, new InMemoryPointsTransactionRepository(), counterRepo,
                tierRepository, new InMemoryTierPolicyRepository(),
                new InMemoryRuleCache(), eventPublisher, null, null, null);

        goldService.processEvent(new IncomingEvent("purchase", t1, u1, "evt-gold", Instant.now(), Map.of()));

        PointsAccount account = pointsRepo.findByMemberId(t1, u1).orElseThrow();
        assertEquals(1000, account.getAvailablePoints());
    }

    @Test
    void creditPoints_manualAdjustment_increasesBalanceAndRecordsTransaction() {
        TenantId t1 = new TenantId(UUID.randomUUID());
        UserId u1 = new UserId(UUID.randomUUID());

        PointsAccount updated = service.creditPoints(t1, u1, 200, "Geste commercial");

        assertEquals(200, updated.getAvailablePoints());
        PointsAccount stored = pointsRepo.findByMemberId(t1, u1).orElseThrow();
        assertEquals(200, stored.getAvailablePoints());
    }

    @Test
    void debitPoints_manualAdjustment_decreasesBalance() {
        TenantId t1 = new TenantId(UUID.randomUUID());
        UserId u1 = new UserId(UUID.randomUUID());
        service.creditPoints(t1, u1, 200, "Solde initial");

        PointsAccount updated = service.debitPoints(t1, u1, 80, "Correction");

        assertEquals(120, updated.getAvailablePoints());
    }

    @Test
    void debitPoints_insufficientBalance_throws() {
        TenantId t1 = new TenantId(UUID.randomUUID());
        UserId u1 = new UserId(UUID.randomUUID());
        service.creditPoints(t1, u1, 50, "Solde initial");

        assertThrows(com.yowyob.loyalty.domain.loyalty.exception.LoyaltyDomainException.class,
                () -> service.debitPoints(t1, u1, 100, "Trop"));
    }

    // --- In Memory Fake Implementations ---

    static class InMemoryRuleRepository implements RuleRepository {
        List<Rule> rules = new ArrayList<>();
        @Override public Rule save(Rule rule) { rules.add(rule); return rule; }
        @Override public Optional<Rule> findById(UUID id) { return rules.stream().filter(r -> r.getId().equals(id)).findFirst(); }
        @Override public List<Rule> findActiveRulesByTenantAndEvent(TenantId t, String e) {
            return rules.stream()
                    .filter(r -> r.getTenantId().equals(t) && r.getTrigger().eventType().equals(e) && r.getStatus() == RuleStatus.ACTIVE)
                    .toList();
        }
        @Override public List<Rule> findByTenant(TenantId t) {
            return rules.stream().filter(r -> r.getTenantId().equals(t)).toList();
        }
    }

    static class InMemoryPointsAccountRepository implements PointsAccountRepository {
        Map<String, PointsAccount> accounts = new HashMap<>();
        @Override public PointsAccount save(PointsAccount account) { accounts.put(account.getMemberId().value().toString(), account); return account; }
        @Override public Optional<PointsAccount> findById(UUID id) { return Optional.empty(); }
        @Override public Optional<PointsAccount> findByMemberId(TenantId tenantId, UserId memberId) { return Optional.ofNullable(accounts.get(memberId.value().toString())); }
    }

    static class InMemoryPointsTransactionRepository implements PointsTransactionRepository {
        List<PointsTransaction> txs = new ArrayList<>();
        @Override public PointsTransaction save(PointsTransaction tx) { txs.add(tx); return tx; }
        @Override public List<PointsTransaction> findByAccountId(UUID id, int limit, int off) { return List.of(); }
        @Override public boolean existsByEventIdempotencyKey(TenantId t, String k) { return txs.stream().anyMatch(tx -> k.equals(tx.eventIdempotencyKey())); }
        @Override public List<PointsTransaction> findByTenantId(TenantId t, int page, int size) {
            return txs.stream().filter(tx -> tx.tenantId().equals(t)).toList();
        }
        @Override public List<ApiKeyPointsFlow> aggregateFlowByApiKey(TenantId t) { return List.of(); }
    }

    static class InMemoryCounterRepository implements CounterRepository {
        Map<String, Counter> counters = new HashMap<>();
        @Override public Counter save(Counter counter) { counters.put(counter.counterKey(), counter); return counter; }
        @Override public Optional<Counter> findByKey(TenantId t, UserId u, String k) { return Optional.ofNullable(counters.get(k)); }
        @Override public List<Counter> findAllByMember(TenantId t, UserId u) { return new ArrayList<>(counters.values()); }
    }

    static class InMemoryMemberTierRepository implements MemberTierRepository {
        MemberTier tier;

        @Override public MemberTier save(MemberTier tier) { this.tier = tier; return tier; }
        @Override public Optional<MemberTier> findByMemberId(TenantId t, UserId u) { return Optional.ofNullable(tier); }
        @Override public List<MemberTier> findAllAboveBronze() {
            return tier != null && tier.level() != com.yowyob.loyalty.domain.loyalty.model.tier.TierLevel.BRONZE
                    ? List.of(tier) : List.of();
        }
    }

    static class InMemoryTierPolicyRepository implements TierPolicyRepository {
        @Override public reactor.core.publisher.Mono<TierPolicy> findByTenantId(TenantId t) { return reactor.core.publisher.Mono.empty(); }
        @Override public reactor.core.publisher.Mono<TierPolicy> save(TierPolicy p) { return reactor.core.publisher.Mono.just(p); }
    }

    static class InMemoryRuleCache implements RuleCachePort {
        @Override public List<Rule> getCachedRules(TenantId t, String e) { return null; }
        @Override public void cacheRules(TenantId t, String e, List<Rule> r) {}
        @Override public void invalidateCache(TenantId t) {}
    }

    static class InMemoryEventPublisher implements LoyaltyEventPublisherPort {
        List<EventProcessingResult> published = new ArrayList<>();
        @Override public void publishProcessedEvent(EventProcessingResult result) { published.add(result); }
    }
}
