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
                new TierEvaluator()
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
}
