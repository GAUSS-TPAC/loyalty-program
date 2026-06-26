package com.yowyob.loyalty.domain.reward.exception;

import java.util.Map;

public class InsufficientPointsException extends RewardDomainException {
    public InsufficientPointsException(long required, long available) {
        super("Points insuffisants pour l'échange",
                Map.of("required", required, "available", available));
    }
}
