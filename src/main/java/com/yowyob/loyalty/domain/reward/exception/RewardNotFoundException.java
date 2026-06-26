package com.yowyob.loyalty.domain.reward.exception;

import java.util.Map;
import java.util.UUID;

public class RewardNotFoundException extends RewardDomainException {
    public RewardNotFoundException(UUID rewardId) {
        super("Récompense introuvable : " + rewardId, Map.of("rewardId", rewardId.toString()));
    }
}
