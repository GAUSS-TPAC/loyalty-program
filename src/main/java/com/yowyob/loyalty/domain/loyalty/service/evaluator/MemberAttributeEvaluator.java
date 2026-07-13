package com.yowyob.loyalty.domain.loyalty.service.evaluator;

import com.yowyob.loyalty.domain.loyalty.model.engine.ConditionEvaluationResult;
import com.yowyob.loyalty.domain.loyalty.model.engine.EvaluationContext;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionType;

import java.math.BigDecimal;

public class MemberAttributeEvaluator implements ConditionEvaluator {

    @Override
    public boolean supports(ConditionType type) {
        return type == ConditionType.MEMBER_ATTRIBUTE;
    }

    @Override
    public ConditionEvaluationResult evaluate(ConditionDefinition condition, EvaluationContext context) {
        String attributeKey = condition.counterKey();
        if (attributeKey == null || attributeKey.isBlank()) {
            return ConditionEvaluationResult.failed(condition.type().name(), "Missing attribute key for MEMBER_ATTRIBUTE");
        }

        Object actualValue = context.event().getPayloadValue(attributeKey).orElse(null);
        if (actualValue == null) {
            return ConditionEvaluationResult.failed(condition.type().name(),
                    "Attribute '" + attributeKey + "' not present in event payload");
        }

        BigDecimal actualDecimal = toBigDecimal(actualValue);
        BigDecimal thresholdDecimal = condition.getThresholdAsBigDecimal();

        boolean passed;
        if (actualDecimal != null && thresholdDecimal != null) {
            passed = condition.operator().evaluate(actualDecimal, thresholdDecimal);
        } else {
            Object threshold = condition.thresholdValue();
            passed = threshold != null
                    && condition.operator().evaluate(actualValue.toString(), threshold.toString());
        }

        if (passed) {
            return ConditionEvaluationResult.passed(condition.type().name());
        }

        return ConditionEvaluationResult.failed(condition.type().name(),
                String.format("Attribute '%s' value %s does not meet condition %s %s",
                        attributeKey, actualValue, condition.operator().name(), condition.thresholdValue()));
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
