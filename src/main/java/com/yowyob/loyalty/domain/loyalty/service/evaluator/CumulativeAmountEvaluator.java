package com.yowyob.loyalty.domain.loyalty.service.evaluator;

import com.yowyob.loyalty.domain.loyalty.model.engine.ConditionEvaluationResult;
import com.yowyob.loyalty.domain.loyalty.model.engine.EvaluationContext;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionType;

import java.math.BigDecimal;

public class CumulativeAmountEvaluator implements ConditionEvaluator {

    @Override
    public boolean supports(ConditionType type) {
        return type == ConditionType.CUMULATIVE_AMOUNT;
    }

    @Override
    public ConditionEvaluationResult evaluate(ConditionDefinition condition, EvaluationContext context) {
        String counterKey = condition.counterKey();
        if (counterKey == null) {
            return ConditionEvaluationResult.failed(condition.type().name(), "Missing counter key for CUMULATIVE_AMOUNT");
        }

        long actualAmountLong = context.getCounterValue(counterKey);
        BigDecimal actualAmount = BigDecimal.valueOf(actualAmountLong);
        BigDecimal threshold = condition.getThresholdAsBigDecimal();

        if (threshold == null) {
            return ConditionEvaluationResult.failed(condition.type().name(), "Invalid threshold for CUMULATIVE_AMOUNT");
        }

        if (condition.operator().evaluate(actualAmount, threshold)) {
            return ConditionEvaluationResult.passed(condition.type().name());
        }

        return ConditionEvaluationResult.failed(condition.type().name(),
                String.format("Actual amount %s does not meet condition %s %s", actualAmount, condition.operator().name(), threshold));
    }
}
