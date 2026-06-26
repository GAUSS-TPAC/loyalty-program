package com.yowyob.loyalty.domain.reward.exception;

import java.util.Map;
import java.util.UUID;

public class GrantNotFoundException extends RewardDomainException {
    public GrantNotFoundException(UUID grantId) {
        super("Grant introuvable : " + grantId, Map.of("grantId", grantId.toString()));
    }
}
