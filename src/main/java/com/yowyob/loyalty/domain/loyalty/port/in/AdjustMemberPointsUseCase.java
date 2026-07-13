package com.yowyob.loyalty.domain.loyalty.port.in;

import com.yowyob.loyalty.domain.loyalty.model.points.PointsAccount;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

public interface AdjustMemberPointsUseCase {
    PointsAccount creditPoints(TenantId tenantId, UserId memberId, long amount, String reason);

    PointsAccount debitPoints(TenantId tenantId, UserId memberId, long amount, String reason);
}
