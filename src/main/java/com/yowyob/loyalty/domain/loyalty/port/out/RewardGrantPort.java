package com.yowyob.loyalty.domain.loyalty.port.out;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

public interface RewardGrantPort {
    void grantReward(TenantId tenantId, UserId memberId, String rewardId, double amount);
}
