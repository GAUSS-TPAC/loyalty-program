package com.yowyob.loyalty.domain.loyalty.model.rule;

import java.util.List;

public enum ConditionOperator {
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL,
    IN,
    NOT_IN;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean evaluate(Comparable actual, Comparable threshold) {
        if (actual == null || threshold == null) {
            return false;
        }
        
        if (this == IN || this == NOT_IN) {
            if (!(threshold instanceof List)) {
                return false;
            }
            List<?> list = (List<?>) threshold;
            boolean contains = list.contains(actual);
            return this == IN ? contains : !contains;
        }

        int comparison = actual.compareTo(threshold);
        return switch (this) {
            case EQUALS -> comparison == 0;
            case NOT_EQUALS -> comparison != 0;
            case GREATER_THAN -> comparison > 0;
            case GREATER_THAN_OR_EQUAL -> comparison >= 0;
            case LESS_THAN -> comparison < 0;
            case LESS_THAN_OR_EQUAL -> comparison <= 0;
            default -> false;
        };
    }
}
