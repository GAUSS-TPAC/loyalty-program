package com.yowyob.loyalty.domain.loyalty.model.engine;

import com.yowyob.loyalty.domain.loyalty.model.event.AppliedEffect;
import com.yowyob.loyalty.domain.loyalty.model.rule.Rule;

import java.util.List;

public record RuleEvaluationResult(
        Rule rule,
        boolean triggered,
        List<ConditionEvaluationResult> conditionResults,
        List<AppliedEffect> appliedEffects,
        String skipReason
) {
    public static RuleEvaluationResult triggered(Rule rule, List<ConditionEvaluationResult> conditionResults, List<AppliedEffect> appliedEffects) {
        return new RuleEvaluationResult(rule, true, conditionResults, appliedEffects, null);
    }

    public static RuleEvaluationResult skipped(Rule rule, String skipReason) {
        return new RuleEvaluationResult(rule, false, List.of(), List.of(), skipReason);
    }

    public static RuleEvaluationResult conditionsFailed(Rule rule, List<ConditionEvaluationResult> conditionResults) {
        return new RuleEvaluationResult(rule, false, conditionResults, List.of(), "conditions_failed");
    }
}
