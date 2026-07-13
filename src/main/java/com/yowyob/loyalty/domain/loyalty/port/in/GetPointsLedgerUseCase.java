package com.yowyob.loyalty.domain.loyalty.port.in;

import com.yowyob.loyalty.domain.loyalty.model.points.ApiKeyPointsFlow;
import com.yowyob.loyalty.domain.loyalty.model.points.PointsLedgerEntry;
import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.util.List;

public interface GetPointsLedgerUseCase {
    List<PointsLedgerEntry> getTenantLedger(TenantId tenantId, int page, int size);
    List<ApiKeyPointsFlow> getFlowByApiKey(TenantId tenantId);
}
