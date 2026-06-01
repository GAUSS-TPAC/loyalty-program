package com.yowyob.loyaulty.program.api.reward.dto.response;

import com.yowyob.loyaulty.program.domain.reward.model.RewardGrant;

import java.time.Instant;
import java.util.UUID;

public record RewardGrantResponse(
        UUID id,
        UUID rewardId,
        String memberId,
        String status,
        Instant grantedAt,
        Instant expiresAt,
        Instant usedAt
) {
    public static RewardGrantResponse from(RewardGrant g) {
        return new RewardGrantResponse(
                g.getId(), g.getRewardId(), g.getMemberId(),
                g.getStatus().name(), g.getGrantedAt(),
                g.getExpiresAt(), g.getUsedAt()
        );
    }
}
