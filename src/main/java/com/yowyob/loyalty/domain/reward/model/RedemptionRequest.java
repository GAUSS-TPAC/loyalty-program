package com.yowyob.loyalty.domain.reward.model;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.util.Objects;
import java.util.UUID;

public record RedemptionRequest(
        TenantId tenantId,
        UserId memberId,
        UUID rewardId,
        String idempotencyKey
) {
    public RedemptionRequest {
        Objects.requireNonNull(tenantId, "tenantId obligatoire");
        Objects.requireNonNull(memberId, "memberId obligatoire");
        Objects.requireNonNull(rewardId, "rewardId obligatoire");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey obligatoire");
    }
}
