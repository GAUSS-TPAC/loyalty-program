package com.yowyob.loyalty.domain.loyalty.service.evaluator;

import com.yowyob.loyalty.domain.loyalty.model.engine.ConditionEvaluationResult;
import com.yowyob.loyalty.domain.loyalty.model.engine.EvaluationContext;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionType;

public class PointsBalanceEvaluator implements ConditionEvaluator {

    @Override
    public boolean supports(ConditionType type) {
        return type == ConditionType.POINTS_BALANCE;
    }

    @Override
    public ConditionEvaluationResult evaluate(ConditionDefinition condition, EvaluationContext context) {
        if (context.pointsAccount() == null) {
            return ConditionEvaluationResult.failed(condition.type().name(), "Aucun compte de points");
        }

        long actualBalance = context.pointsAccount().getAvailablePoints();
        Long threshold = condition.getThresholdAsLong();

        if (threshold == null) {
            return ConditionEvaluationResult.failed(condition.type().name(), "Invalid threshold for POINTS_BALANCE");
        }

        if (condition.operator().evaluate(actualBalance, threshold)) {
            return ConditionEvaluationResult.passed(condition.type().name());
        }

        return ConditionEvaluationResult.failed(condition.type().name(),
                String.format("Actual balance %d does not meet condition %s %d", actualBalance, condition.operator().name(), threshold));
    }
}
