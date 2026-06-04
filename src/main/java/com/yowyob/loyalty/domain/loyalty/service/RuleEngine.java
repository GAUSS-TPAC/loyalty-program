package com.yowyob.loyalty.domain.loyalty.service;

import com.yowyob.loyalty.domain.loyalty.model.engine.ConditionEvaluationResult;
import com.yowyob.loyalty.domain.loyalty.model.engine.EvaluationContext;
import com.yowyob.loyalty.domain.loyalty.model.engine.RuleEvaluationResult;
import com.yowyob.loyalty.domain.loyalty.model.event.AppliedEffect;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.EffectDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.Rule;
import com.yowyob.loyalty.domain.loyalty.service.evaluator.ConditionEvaluator;
import com.yowyob.loyalty.domain.loyalty.service.executor.EffectExecutionContext;
import com.yowyob.loyalty.domain.loyalty.service.executor.EffectExecutor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RuleEngine {

    private final List<ConditionEvaluator> evaluators;
    private final List<EffectExecutor> executors;

    public RuleEngine(List<ConditionEvaluator> evaluators, List<EffectExecutor> executors) {
        this.evaluators = evaluators;
        this.executors = executors;
    }

    public List<RuleEvaluationResult> process(List<Rule> activeRules, EvaluationContext context, EffectExecutionContext effectContext) {
        // Sort rules by priority descending
        List<Rule> sortedRules = activeRules.stream()
                .filter(r -> r.isActiveAt(context.event().occurredAt()))
                .sorted(Comparator.comparingInt(Rule::getPriority).reversed())
                .collect(Collectors.toList());

        List<RuleEvaluationResult> results = new ArrayList<>();

        for (Rule rule : sortedRules) {
            if (!rule.triggerMatches(context.event())) {
                results.add(RuleEvaluationResult.skipped(rule, "trigger_mismatch"));
                continue;
            }

            List<ConditionEvaluationResult> conditionResults = new ArrayList<>();
            boolean allConditionsPassed = true;

            for (ConditionDefinition condition : rule.getConditions()) {
                ConditionEvaluator evaluator = evaluators.stream()
                        .filter(e -> e.supports(condition.type()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No evaluator found for condition: " + condition.type()));

                ConditionEvaluationResult result = evaluator.evaluate(condition, context);
                conditionResults.add(result);

                if (!result.passed()) {
                    allConditionsPassed = false;
                    break; // Fail fast
                }
            }

            if (!allConditionsPassed) {
                results.add(RuleEvaluationResult.conditionsFailed(rule, conditionResults));
                continue;
            }

            List<AppliedEffect> appliedEffects = new ArrayList<>();
            for (EffectDefinition effect : rule.getEffects()) {
                EffectExecutor executor = executors.stream()
                        .filter(e -> e.supports(effect.type()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No executor found for effect: " + effect.type()));

                AppliedEffect applied = executor.execute(effect, context, effectContext, rule.getId(), rule.getName());
                if (applied != null) {
                    appliedEffects.add(applied);
                }
            }

            results.add(RuleEvaluationResult.triggered(rule, conditionResults, appliedEffects));
        }

        return results;
    }
}
