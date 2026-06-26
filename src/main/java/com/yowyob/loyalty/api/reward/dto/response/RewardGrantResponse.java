package com.yowyob.loyalty.api.reward.dto.response;

import com.yowyob.loyalty.domain.reward.model.RewardGrant;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RewardGrantResponse(
        UUID id,
        String rewardName,
        String rewardType,
        BigDecimal numericValue,
        String valueUnit,
        String status,
        int remainingApplications,
        Instant grantedAt,
        Instant expiresAt
) {
    public static RewardGrantResponse from(RewardGrant grant) {
        return new RewardGrantResponse(
                grant.id(),
                grant.rewardName(),
                grant.rewardType().name(),
                grant.rewardValue().numericValue(),
                grant.rewardValue().unit(),
                grant.status().name(),
                grant.remainingApplications(),
                grant.grantedAt(),
                grant.expiresAt()
        );
    }
}
