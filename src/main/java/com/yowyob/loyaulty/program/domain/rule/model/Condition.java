package com.yowyob.loyaulty.program.domain.rule.model;

import com.yowyob.loyaulty.program.domain.rule.model.enums.ConditionType;

import java.math.BigDecimal;

public record Condition(
        ConditionType type,
        String operator,
        BigDecimal value,
        String window
) {
    public boolean evaluate(long counterValue) {
        return compare(counterValue, operator, value.longValue());
    }

    public boolean evaluateAmount(BigDecimal actual) {
        return switch (operator) {
            case "gte", ">=" -> actual.compareTo(value) >= 0;
            case "gt",  ">"  -> actual.compareTo(value) > 0;
            case "lte", "<=" -> actual.compareTo(value) <= 0;
            case "lt",  "<"  -> actual.compareTo(value) < 0;
            case "eq",  "="  -> actual.compareTo(value) == 0;
            default -> false;
        };
    }

    private boolean compare(long actual, String op, long threshold) {
        return switch (op) {
            case "gte", ">=" -> actual >= threshold;
            case "gt",  ">"  -> actual > threshold;
            case "lte", "<=" -> actual <= threshold;
            case "lt",  "<"  -> actual < threshold;
            case "eq",  "="  -> actual == threshold;
            default -> false;
        };
    }
}
