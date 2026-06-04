package com.yowyob.loyalty.domain.loyalty.port.in;

import com.yowyob.loyalty.domain.loyalty.model.points.PointsAccount;
import com.yowyob.loyalty.domain.loyalty.model.points.PointsTransaction;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.util.List;

public interface GetMemberPointsUseCase {
    PointsAccount getPoints(TenantId tenantId, UserId memberId);

    List<PointsTransaction> getPointsHistory(TenantId tenantId, UserId memberId, int page, int size);
}
