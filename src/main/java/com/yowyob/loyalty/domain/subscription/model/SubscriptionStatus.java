package com.yowyob.loyalty.domain.subscription.model;

public enum SubscriptionStatus {
    TRIAL,
    ACTIVE,
    PAST_DUE,
    CANCELLED,
    EXPIRED;

    public boolean isTerminal() {
        return this == CANCELLED || this == EXPIRED;
    }

    public boolean isAccessible() {
        return this == TRIAL || this == ACTIVE;
    }
}
