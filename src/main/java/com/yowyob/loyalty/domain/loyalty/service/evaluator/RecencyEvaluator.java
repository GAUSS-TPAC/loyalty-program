package com.yowyob.loyalty.domain.loyalty.service.evaluator;

import com.yowyob.loyalty.domain.loyalty.model.counter.Counter;
import com.yowyob.loyalty.domain.loyalty.model.engine.ConditionEvaluationResult;
import com.yowyob.loyalty.domain.loyalty.model.engine.EvaluationContext;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionType;

import java.time.temporal.ChronoUnit;

public class RecencyEvaluator implements ConditionEvaluator {

    @Override
    public boolean supports(ConditionType type) {
        return type == ConditionType.RECENCY;
    }

    @Override
    public ConditionEvaluationResult evaluate(ConditionDefinition condition, EvaluationContext context) {
        String counterKey = condition.counterKey();
        if (counterKey == null) {
            return ConditionEvaluationResult.failed(condition.type().name(), "Missing counter key for RECENCY");
        }

        Long thresholdDays = condition.getThresholdAsLong();
        if (thresholdDays == null) {
            return ConditionEvaluationResult.failed(condition.type().name(), "Invalid threshold for RECENCY");
        }

        // Measure against the counter state prior to this event: the trigger stamping
        // done before evaluation would otherwise make "days since last activity" always 0.
        Counter counter = context.getCounterBeforeEvent(counterKey).orElse(null);
        if (counter == null || counter.updatedAt() == null) {
            return ConditionEvaluationResult.failed(condition.type().name(),
                    "No prior activity recorded for counter '" + counterKey + "'");
        }

        long daysSinceLastActivity = ChronoUnit.DAYS.between(counter.updatedAt(), context.event().occurredAt());
        if (daysSinceLastActivity < 0) {
            daysSinceLastActivity = 0;
        }

        if (condition.operator().evaluate(daysSinceLastActivity, thresholdDays)) {
            return ConditionEvaluationResult.passed(condition.type().name());
        }

        return ConditionEvaluationResult.failed(condition.type().name(),
                String.format("Days since last activity %d does not meet condition %s %d",
                        daysSinceLastActivity, condition.operator().name(), thresholdDays));
    }
}
