package com.yowyob.loyaulty.program.domain.member.model.enums;

public enum TierLevel {
    BRONZE(1.0),
    SILVER(1.5),
    GOLD(2.0),
    PLATINUM(3.0);

    private final double pointsMultiplier;

    TierLevel(double pointsMultiplier) {
        this.pointsMultiplier = pointsMultiplier;
    }

    public double getPointsMultiplier() {
        return pointsMultiplier;
    }
}
