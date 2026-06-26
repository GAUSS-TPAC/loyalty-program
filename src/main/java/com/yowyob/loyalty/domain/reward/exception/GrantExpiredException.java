package com.yowyob.loyalty.domain.reward.exception;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class GrantExpiredException extends RewardDomainException {
    public GrantExpiredException(UUID grantId, Instant expiredAt) {
        super("Ce grant est expiré",
                Map.of("grantId", grantId.toString(), "expiredAt", expiredAt.toString()));
    }
}
