package com.yowyob.loyalty.domain.loyalty.service.evaluator;

import com.yowyob.loyalty.domain.loyalty.model.engine.ConditionEvaluationResult;
import com.yowyob.loyalty.domain.loyalty.model.engine.EvaluationContext;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionType;

public interface ConditionEvaluator {
    boolean supports(ConditionType type);
    ConditionEvaluationResult evaluate(ConditionDefinition condition, EvaluationContext context);
}
