package com.yowyob.loyalty.domain.loyalty.service.evaluator;

import com.yowyob.loyalty.domain.loyalty.model.engine.ConditionEvaluationResult;
import com.yowyob.loyalty.domain.loyalty.model.engine.EvaluationContext;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionOperator;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionType;
import com.yowyob.loyalty.domain.loyalty.model.tier.TierLevel;

public class TierEvaluator implements ConditionEvaluator {

    @Override
    public boolean supports(ConditionType type) {
        return type == ConditionType.TIER_IS;
    }

    @Override
    public ConditionEvaluationResult evaluate(ConditionDefinition condition, EvaluationContext context) {
        if (context.memberTier() == null) {
            return ConditionEvaluationResult.failed(condition.type().name(), "Membre sans palier assigné");
        }

        String thresholdStr = condition.thresholdValue() != null ? condition.thresholdValue().toString() : null;
        if (thresholdStr == null) {
            return ConditionEvaluationResult.failed(condition.type().name(), "Invalid threshold for TIER_IS");
        }

        TierLevel thresholdTier;
        try {
            thresholdTier = TierLevel.valueOf(thresholdStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ConditionEvaluationResult.failed(condition.type().name(), "Unknown tier level: " + thresholdStr);
        }

        TierLevel actualTier = context.memberTier().level();
        boolean passed;

        if (condition.operator() == ConditionOperator.EQUALS) {
            passed = actualTier == thresholdTier;
        } else if (condition.operator() == ConditionOperator.GREATER_THAN) {
            passed = actualTier.isHigherThan(thresholdTier);
        } else if (condition.operator() == ConditionOperator.GREATER_THAN_OR_EQUAL) {
            passed = actualTier == thresholdTier || actualTier.isHigherThan(thresholdTier);
        } else if (condition.operator() == ConditionOperator.NOT_EQUALS) {
            passed = actualTier != thresholdTier;
        } else {
            return ConditionEvaluationResult.failed(condition.type().name(), "Unsupported operator for TIER_IS");
        }

        if (passed) {
            return ConditionEvaluationResult.passed(condition.type().name());
        }

        return ConditionEvaluationResult.failed(condition.type().name(),
                String.format("Actual tier %s does not meet condition %s %s", actualTier, condition.operator().name(), thresholdTier));
    }
}
