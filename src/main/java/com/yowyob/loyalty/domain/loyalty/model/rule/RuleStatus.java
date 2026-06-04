package com.yowyob.loyalty.domain.loyalty.model.rule;

public enum RuleStatus {
    DRAFT,
    ACTIVE,
    SUSPENDED,
    ARCHIVED;

    public boolean isEvaluable() {
        return this == ACTIVE;
    }
}
