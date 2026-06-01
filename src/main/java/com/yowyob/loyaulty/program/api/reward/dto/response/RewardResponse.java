package com.yowyob.loyaulty.program.api.reward.dto.response;

import com.yowyob.loyaulty.program.domain.reward.model.Reward;

import java.util.UUID;

public record RewardResponse(
        UUID id,
        String name,
        String description,
        String type,
        long costPoints,
        Integer stock,
        String status
) {
    public static RewardResponse from(Reward r) {
        return new RewardResponse(
                r.getId(), r.getName(), r.getDescription(),
                r.getType().name(), r.getCostPoints(), r.getStock(),
                r.getStatus().name()
        );
    }
}
