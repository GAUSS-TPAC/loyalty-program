package com.yowyob.loyalty.domain.loyalty.port.in;

import com.yowyob.loyalty.domain.loyalty.model.points.PointsTransaction;
import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.util.List;

public interface GetPointsLedgerUseCase {
    List<PointsTransaction> getTenantLedger(TenantId tenantId, int page, int size);
}
