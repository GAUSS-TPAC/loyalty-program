package com.yowyob.loyalty.domain.reward.exception;

import java.util.Map;
import java.util.UUID;

public class GrantAlreadyUsedException extends RewardDomainException {
    public GrantAlreadyUsedException(UUID grantId) {
        super("Ce grant a déjà été utilisé", Map.of("grantId", grantId.toString()));
    }
}
