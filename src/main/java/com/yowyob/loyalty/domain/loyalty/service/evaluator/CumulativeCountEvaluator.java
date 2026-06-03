package com.yowyob.loyalty.domain.loyalty.service.evaluator;

import com.yowyob.loyalty.domain.loyalty.model.engine.ConditionEvaluationResult;
import com.yowyob.loyalty.domain.loyalty.model.engine.EvaluationContext;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionType;

public class CumulativeCountEvaluator implements ConditionEvaluator {

    @Override
    public boolean supports(ConditionType type) {
        return type == ConditionType.CUMULATIVE_COUNT;
    }

    @Override
    public ConditionEvaluationResult evaluate(ConditionDefinition condition, EvaluationContext context) {
        String counterKey = condition.counterKey();
        if (counterKey == null) {
            return ConditionEvaluationResult.failed(condition.type().name(), "Missing counter key for CUMULATIVE_COUNT");
        }

        long actualCount = context.getCounterValue(counterKey);
        Long threshold = condition.getThresholdAsLong();

        if (threshold == null) {
            return ConditionEvaluationResult.failed(condition.type().name(), "Invalid threshold for CUMULATIVE_COUNT");
        }

        if (condition.operator().evaluate(actualCount, threshold)) {
            return ConditionEvaluationResult.passed(condition.type().name());
        }

        return ConditionEvaluationResult.failed(condition.type().name(),
                String.format("Actual count %d does not meet condition %s %d", actualCount, condition.operator().name(), threshold));
    }
}
