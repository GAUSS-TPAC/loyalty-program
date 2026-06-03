package com.yowyob.loyalty.domain.loyalty;

import com.yowyob.loyalty.domain.loyalty.model.counter.Counter;
import com.yowyob.loyalty.domain.loyalty.model.event.EventProcessingResult;
import com.yowyob.loyalty.domain.loyalty.model.event.IncomingEvent;
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
                new InMemoryRuleCache(), eventPublisher, null);
    }

    @Test
    void processEvent_creditsPointsOnConditionMet() {
        TenantId t1 = new TenantId(java.util.UUID.randomUUID());
        UserId u1 = new UserId(java.util.UUID.randomUUID());

        Rule rule = Rule.create(UUID.randomUUID(), t1, "Bonus", "Desc",
                new TriggerDefinition("purchase", null),
                List.of(new ConditionDefinition(ConditionType.CUMULATIVE_COUNT, ConditionOperator.GREATER_THAN_OR_EQUAL, 3, "LIFETIME", "purchases")),
                List.of(new EffectDefinition(EffectType.CREDIT_POINTS, Map.of("amount", 100))),
                10, null, null).activate();
        ruleRepo.rules.add(rule);

        // We pre-set the counter to 3, representing this is the 3rd purchase (handled external to domain rules normally, or by another effect)
        counterRepo.save(new Counter(UUID.randomUUID(), t1, u1, "purchases", 3, "LIFETIME", Instant.now(), Instant.now()));

        IncomingEvent event = new IncomingEvent("purchase", t1, u1, "evt1", Instant.now(), Map.of());
        EventProcessingResult result = service.processEvent(event);

        assertTrue(result.hasEffects());
        assertEquals(1, result.effectsApplied().size());
        assertEquals("CREDIT_POINTS", result.effectsApplied().get(0).effectType());
        
        PointsAccount account = pointsRepo.findByMemberId(t1, u1).orElseThrow();
        assertEquals(100, account.getAvailablePoints());
    }

    // --- In Memory Fake Implementations ---

    static class InMemoryRuleRepository implements RuleRepository {
        List<Rule> rules = new ArrayList<>();
        @Override public Rule save(Rule rule) { rules.add(rule); return rule; }
        @Override public Optional<Rule> findById(UUID id) { return rules.stream().filter(r -> r.getId().equals(id)).findFirst(); }
        @Override public List<Rule> findActiveRulesByTenantAndEvent(TenantId t, String e) {
            return rules.stream().filter(r -> r.getTenantId().equals(t) && r.getTrigger().eventType().equals(e)).toList();
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
    }

    static class InMemoryCounterRepository implements CounterRepository {
        Map<String, Counter> counters = new HashMap<>();
        @Override public Counter save(Counter counter) { counters.put(counter.counterKey(), counter); return counter; }
        @Override public Optional<Counter> findByKey(TenantId t, UserId u, String k) { return Optional.ofNullable(counters.get(k)); }
        @Override public List<Counter> findAllByMember(TenantId t, UserId u) { return new ArrayList<>(counters.values()); }
    }

    static class InMemoryMemberTierRepository implements MemberTierRepository {
        @Override public MemberTier save(MemberTier tier) { return tier; }
        @Override public Optional<MemberTier> findByMemberId(TenantId t, UserId u) { return Optional.empty(); }
    }

    static class InMemoryTierPolicyRepository implements TierPolicyRepository {
        @Override public Optional<TierPolicy> findByTenantId(TenantId t) { return Optional.empty(); }
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
