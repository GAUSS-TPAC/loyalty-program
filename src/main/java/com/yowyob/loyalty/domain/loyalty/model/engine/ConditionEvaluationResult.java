package com.yowyob.loyalty.domain.loyalty.model.engine;

public record ConditionEvaluationResult(
        boolean passed,
        String conditionType,
        String reason
) {
    public static ConditionEvaluationResult passed(String conditionType) {
        return new ConditionEvaluationResult(true, conditionType, null);
    }

    public static ConditionEvaluationResult failed(String conditionType, String reason) {
        return new ConditionEvaluationResult(false, conditionType, reason);
    }
}
