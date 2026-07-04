package com.yowyob.loyalty.domain.loyalty.port.out;

import com.yowyob.loyalty.domain.loyalty.model.points.PointsTransaction;
import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.util.List;
import java.util.UUID;

public interface PointsTransactionRepository {
    PointsTransaction save(PointsTransaction transaction);
    List<PointsTransaction> findByAccountId(UUID accountId, int limit, int offset);
    boolean existsByEventIdempotencyKey(TenantId tenantId, String idempotencyKey);
    List<PointsTransaction> findByTenantId(TenantId tenantId, int page, int size);
}
