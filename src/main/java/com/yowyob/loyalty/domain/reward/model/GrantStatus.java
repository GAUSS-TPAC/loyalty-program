package com.yowyob.loyalty.domain.reward.model;

public enum GrantStatus {
    PENDING,
    ACTIVE,
    USED,
    EXPIRED,
    REVERSED,
    CANCELLED;

    public boolean isFinal() {
        return this == USED || this == EXPIRED || this == REVERSED || this == CANCELLED;
    }

    public boolean isUsable() {
        return this == ACTIVE;
    }

    public boolean canTransitionTo(GrantStatus next) {
        return switch (this) {
            case PENDING -> next == ACTIVE || next == CANCELLED || next == REVERSED;
            case ACTIVE -> next == USED || next == EXPIRED || next == CANCELLED || next == REVERSED;
            default -> false;
        };
    }
}
