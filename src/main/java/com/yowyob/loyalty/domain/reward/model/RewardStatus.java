package com.yowyob.loyalty.domain.reward.model;

public enum RewardStatus {
    DRAFT,
    ACTIVE,
    PAUSED,
    EXHAUSTED,
    EXPIRED,
    ARCHIVED;

    public boolean isAvailable() {
        return this == ACTIVE;
    }

    public boolean canTransitionTo(RewardStatus next) {
        return switch (this) {
            case DRAFT -> next == ACTIVE || next == ARCHIVED;
            case ACTIVE -> next == PAUSED || next == EXHAUSTED || next == EXPIRED || next == ARCHIVED;
            case PAUSED -> next == ACTIVE || next == ARCHIVED;
            case EXHAUSTED -> next == ACTIVE || next == ARCHIVED;
            case EXPIRED, ARCHIVED -> false;
        };
    }
}
