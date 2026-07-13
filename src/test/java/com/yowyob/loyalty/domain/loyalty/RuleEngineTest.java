package com.yowyob.loyalty.domain.loyalty;

import com.yowyob.loyalty.domain.loyalty.model.counter.Counter;
import com.yowyob.loyalty.domain.loyalty.model.engine.EvaluationContext;
import com.yowyob.loyalty.domain.loyalty.model.engine.RuleEvaluationResult;
import com.yowyob.loyalty.domain.loyalty.model.event.IncomingEvent;
import com.yowyob.loyalty.domain.loyalty.model.points.PointsAccount;
import com.yowyob.loyalty.domain.loyalty.model.rule.*;
import com.yowyob.loyalty.domain.loyalty.model.tier.MemberTier;
import com.yowyob.loyalty.domain.loyalty.model.tier.TierLevel;
import com.yowyob.loyalty.domain.loyalty.service.RuleEngine;
import com.yowyob.loyalty.domain.loyalty.service.evaluator.*;
import com.yowyob.loyalty.domain.loyalty.service.executor.*;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RuleEngineTest {

    private RuleEngine engine;

    @BeforeEach
    void setUp() {
        List<ConditionEvaluator> evaluators = List.of(
                new CumulativeCountEvaluator(),
                new TimeWindowEvaluator(),
                new TierEvaluator(),
                new MemberAttributeEvaluator(),
                new RecencyEvaluator()
        );
        List<EffectExecutor> executors = List.of(
                new CreditPointsExecutor()
        );
        engine = new RuleEngine(evaluators, executors);
    }

    @Test
    void process_triggerMismatch_skipsRule() {
        Rule rule = Rule.create(UUID.randomUUID(), new TenantId(java.util.UUID.randomUUID()), "Test Rule", "Desc",
                new TriggerDefinition("purchase.completed", null),
                List.of(),
                List.of(new EffectDefinition(EffectType.CREDIT_POINTS, Map.of("amount", 500))),
                10, null, null).activate();

        IncomingEvent event = new IncomingEvent("trip.ended", new TenantId(java.util.UUID.randomUUID()), new UserId(java.util.UUID.randomUUID()), "k1", Instant.now(), Map.of());
        EvaluationContext context = new EvaluationContext(event, null, null, Map.of(), null);
        EffectExecutionContext effectContext = new EffectExecutionContext();

        List<RuleEvaluationResult> results = engine.process(List.of(rule), context, effectContext);

        assertEquals(1, results.size());
        assertFalse(results.get(0).triggered());
        assertEquals("trigger_mismatch", results.get(0).skipReason());
        assertTrue(effectContext.getPendingPointsOperations().isEmpty());
    }

    @Test
    void process_conditionFails_skipsRule() {
        Rule rule = Rule.create(UUID.randomUUID(), new TenantId(java.util.UUID.randomUUID()), "Test Rule", "Desc",
                new TriggerDefinition("purchase.completed", null),
                List.of(new ConditionDefinition(ConditionType.CUMULATIVE_COUNT, ConditionOperator.GREATER_THAN_OR_EQUAL, 10, "LIFETIME", "purchases")),
                List.of(new EffectDefinition(EffectType.CREDIT_POINTS, Map.of("amount", 500))),
                10, null, null).activate();

        IncomingEvent event = new IncomingEvent("purchase.completed", new TenantId(java.util.UUID.randomUUID()), new UserId(java.util.UUID.randomUUID()), "k1", Instant.now(), Map.of());
        Counter counter = new Counter(UUID.randomUUID(), new TenantId(java.util.UUID.randomUUID()), new UserId(java.util.UUID.randomUUID()), "purchases", 5, "LIFETIME", Instant.now(), Instant.now());
        EvaluationContext context = new EvaluationContext(event, null, null, Map.of("purchases", counter), null);
        EffectExecutionContext effectContext = new EffectExecutionContext();

        List<RuleEvaluationResult> results = engine.process(List.of(rule), context, effectContext);

        assertEquals(1, results.size());
        assertFalse(results.get(0).triggered());
        assertEquals("conditions_failed", results.get(0).skipReason());
    }

    @Test
    void process_ruleTriggered_appliesEffects() {
        Rule rule = Rule.create(UUID.randomUUID(), new TenantId(java.util.UUID.randomUUID()), "Test Rule", "Desc",
                new TriggerDefinition("purchase.completed", null),
                List.of(new ConditionDefinition(ConditionType.CUMULATIVE_COUNT, ConditionOperator.GREATER_THAN_OR_EQUAL, 10, "LIFETIME", "purchases")),
                List.of(new EffectDefinition(EffectType.CREDIT_POINTS, Map.of("amount", 500))),
                10, null, null).activate();

        IncomingEvent event = new IncomingEvent("purchase.completed", new TenantId(java.util.UUID.randomUUID()), new UserId(java.util.UUID.randomUUID()), "k1", Instant.now(), Map.of());
        Counter counter = new Counter(UUID.randomUUID(), new TenantId(java.util.UUID.randomUUID()), new UserId(java.util.UUID.randomUUID()), "purchases", 10, "LIFETIME", Instant.now(), Instant.now());
        EvaluationContext context = new EvaluationContext(event, null, null, Map.of("purchases", counter), null);
        EffectExecutionContext effectContext = new EffectExecutionContext();

        List<RuleEvaluationResult> results = engine.process(List.of(rule), context, effectContext);

        assertEquals(1, results.size());
        assertTrue(results.get(0).triggered());
        assertEquals(1, results.get(0).appliedEffects().size());
        assertEquals(1, effectContext.getPendingPointsOperations().size());
        assertEquals(500, effectContext.getPendingPointsOperations().get(0).amount());
    }

    @Test
    void process_tierMultiplier_appliesMultiplierToPoints() {
        Rule rule = Rule.create(UUID.randomUUID(), new TenantId(java.util.UUID.randomUUID()), "Test Rule", "Desc",
                new TriggerDefinition("purchase.completed", null),
                List.of(),
                List.of(new EffectDefinition(EffectType.CREDIT_POINTS, Map.of("amount", 500))),
                10, null, null).activate();

        IncomingEvent event = new IncomingEvent("purchase.completed", new TenantId(java.util.UUID.randomUUID()), new UserId(java.util.UUID.randomUUID()), "k1", Instant.now(), Map.of());
        MemberTier tier = new MemberTier(UUID.randomUUID(), new TenantId(java.util.UUID.randomUUID()), new UserId(java.util.UUID.randomUUID()), TierLevel.SILVER, new BigDecimal("1.5"), Instant.now(), null);
        EvaluationContext context = new EvaluationContext(event, null, tier, Map.of(), null);
        EffectExecutionContext effectContext = new EffectExecutionContext();

        engine.process(List.of(rule), context, effectContext);

        assertEquals(1, effectContext.getPendingPointsOperations().size());
        assertEquals(750, effectContext.getPendingPointsOperations().get(0).amount()); // 500 * 1.5
    }

    @Test
    void process_memberAttributeAmountBracket_triggersOnlyWithinRange() {
        Rule rule = Rule.create(UUID.randomUUID(), new TenantId(UUID.randomUUID()), "Amount Bracket", "Desc",
                new TriggerDefinition("purchase.completed", null),
                List.of(
                        new ConditionDefinition(ConditionType.MEMBER_ATTRIBUTE, ConditionOperator.GREATER_THAN_OR_EQUAL, 100, null, "amount"),
                        new ConditionDefinition(ConditionType.MEMBER_ATTRIBUTE, ConditionOperator.LESS_THAN_OR_EQUAL, 500, null, "amount")
                ),
                List.of(new EffectDefinition(EffectType.CREDIT_POINTS, Map.of("amount", 50))),
                10, null, null).activate();

        IncomingEvent inRange = new IncomingEvent("purchase.completed", new TenantId(UUID.randomUUID()), new UserId(UUID.randomUUID()), "k1", Instant.now(), Map.of("amount", 250));
        EffectExecutionContext inRangeEffects = new EffectExecutionContext();
        List<RuleEvaluationResult> inRangeResults = engine.process(List.of(rule),
                new EvaluationContext(inRange, null, null, Map.of(), null), inRangeEffects);
        assertTrue(inRangeResults.get(0).triggered());
        assertEquals(1, inRangeEffects.getPendingPointsOperations().size());

        IncomingEvent outOfRange = new IncomingEvent("purchase.completed", new TenantId(UUID.randomUUID()), new UserId(UUID.randomUUID()), "k2", Instant.now(), Map.of("amount", 50));
        EffectExecutionContext outOfRangeEffects = new EffectExecutionContext();
        List<RuleEvaluationResult> outOfRangeResults = engine.process(List.of(rule),
                new EvaluationContext(outOfRange, null, null, Map.of(), null), outOfRangeEffects);
        assertFalse(outOfRangeResults.get(0).triggered());
        assertTrue(outOfRangeEffects.getPendingPointsOperations().isEmpty());
    }

    @Test
    void process_recencyCondition_passesWithinWindowFailsWhenStale() {
        Rule rule = Rule.create(UUID.randomUUID(), new TenantId(UUID.randomUUID()), "Anti-churn bonus", "Desc",
                new TriggerDefinition("purchase.completed", null),
                List.of(new ConditionDefinition(ConditionType.RECENCY, ConditionOperator.LESS_THAN_OR_EQUAL, 30, null, "last_purchase")),
                List.of(new EffectDefinition(EffectType.CREDIT_POINTS, Map.of("amount", 50))),
                10, null, null).activate();

        Instant now = Instant.now();
        IncomingEvent event = new IncomingEvent("purchase.completed", new TenantId(UUID.randomUUID()), new UserId(UUID.randomUUID()), "k1", now, Map.of());

        Counter recentCounter = new Counter(UUID.randomUUID(), new TenantId(UUID.randomUUID()), new UserId(UUID.randomUUID()),
                "last_purchase", 1, "LIFETIME", now.minus(10, ChronoUnit.DAYS), now.minus(10, ChronoUnit.DAYS));
        EffectExecutionContext recentEffects = new EffectExecutionContext();
        List<RuleEvaluationResult> recentResults = engine.process(List.of(rule),
                new EvaluationContext(event, null, null, Map.of("last_purchase", recentCounter), null), recentEffects);
        assertTrue(recentResults.get(0).triggered());

        Counter staleCounter = new Counter(UUID.randomUUID(), new TenantId(UUID.randomUUID()), new UserId(UUID.randomUUID()),
                "last_purchase", 1, "LIFETIME", now.minus(90, ChronoUnit.DAYS), now.minus(90, ChronoUnit.DAYS));
        EffectExecutionContext staleEffects = new EffectExecutionContext();
        List<RuleEvaluationResult> staleResults = engine.process(List.of(rule),
                new EvaluationContext(event, null, null, Map.of("last_purchase", staleCounter), null), staleEffects);
        assertFalse(staleResults.get(0).triggered());
    }

    @Test
    void process_debitPoints_enqueuesDebitWhenSufficientBalance() {
        Rule rule = Rule.create(UUID.randomUUID(), new TenantId(UUID.randomUUID()), "Redeem for wallet credit", "Desc",
                new TriggerDefinition("redeem.requested", null),
                List.of(),
                List.of(new EffectDefinition(EffectType.DEBIT_POINTS, Map.of("amount", 100))),
                10, null, null).activate();

        IncomingEvent event = new IncomingEvent("redeem.requested", new TenantId(UUID.randomUUID()), new UserId(UUID.randomUUID()), "k1", Instant.now(), Map.of());
        PointsAccount account = PointsAccount.reconstruct(UUID.randomUUID(), new TenantId(UUID.randomUUID()), new UserId(UUID.randomUUID()),
                150, 150, 0, 0, Instant.now(), Instant.now());
        EffectExecutionContext effectContext = new EffectExecutionContext();

        List<RuleEvaluationResult> results = engine.process(List.of(rule),
                new EvaluationContext(event, account, null, Map.of(), null), effectContext);

        assertTrue(results.get(0).triggered());
        assertEquals(1, effectContext.getPendingPointsOperations().size());
        assertEquals("DEBIT", effectContext.getPendingPointsOperations().get(0).type());
        assertEquals(100, effectContext.getPendingPointsOperations().get(0).amount());
    }

    @Test
    void process_debitPoints_skipsWhenInsufficientBalance() {
        Rule rule = Rule.create(UUID.randomUUID(), new TenantId(UUID.randomUUID()), "Redeem for wallet credit", "Desc",
                new TriggerDefinition("redeem.requested", null),
                List.of(),
                List.of(new EffectDefinition(EffectType.DEBIT_POINTS, Map.of("amount", 100))),
                10, null, null).activate();

        IncomingEvent event = new IncomingEvent("redeem.requested", new TenantId(UUID.randomUUID()), new UserId(UUID.randomUUID()), "k1", Instant.now(), Map.of());
        PointsAccount account = PointsAccount.reconstruct(UUID.randomUUID(), new TenantId(UUID.randomUUID()), new UserId(UUID.randomUUID()),
                50, 50, 0, 0, Instant.now(), Instant.now());
        EffectExecutionContext effectContext = new EffectExecutionContext();

        List<RuleEvaluationResult> results = engine.process(List.of(rule),
                new EvaluationContext(event, account, null, Map.of(), null), effectContext);

        // Conditions still pass (there are none), but the executor refuses to enqueue
        // a debit it can't cover instead of letting the balance go negative.
        assertTrue(results.get(0).triggered());
        assertTrue(effectContext.getPendingPointsOperations().isEmpty());
    }
}
