package com.yowyob.loyalty.domain.loyalty.service.evaluator;

import com.yowyob.loyalty.domain.loyalty.model.engine.ConditionEvaluationResult;
import com.yowyob.loyalty.domain.loyalty.model.engine.EvaluationContext;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionType;

public class FirstEventEvaluator implements ConditionEvaluator {

    @Override
    public boolean supports(ConditionType type) {
        return type == ConditionType.FIRST_EVENT;
    }

    @Override
    public ConditionEvaluationResult evaluate(ConditionDefinition condition, EvaluationContext context) {
        String counterKey = condition.counterKey();
        if (counterKey == null) {
            return ConditionEvaluationResult.failed(condition.type().name(), "Missing counter key for FIRST_EVENT");
        }

        long actualCount = context.getCounterValue(counterKey);

        if (actualCount == 0) {
            return ConditionEvaluationResult.passed(condition.type().name());
        }

        return ConditionEvaluationResult.failed(condition.type().name(), "Not the first event (count: " + actualCount + ")");
    }
}
