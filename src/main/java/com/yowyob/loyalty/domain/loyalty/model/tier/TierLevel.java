package com.yowyob.loyalty.domain.loyalty.model.tier;

import java.util.Optional;

public enum TierLevel {
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM;

    public boolean isHigherThan(TierLevel other) {
        if (other == null) return true;
        return this.ordinal() > other.ordinal();
    }

    public Optional<TierLevel> next() {
        if (this == PLATINUM) {
            return Optional.empty();
        }
        return Optional.of(TierLevel.values()[this.ordinal() + 1]);
    }
}
