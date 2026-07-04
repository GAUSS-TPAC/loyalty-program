package com.yowyob.loyalty.application.loyalty.handler;

import com.yowyob.loyalty.domain.loyalty.model.points.PointsTransaction;
import com.yowyob.loyalty.domain.loyalty.port.in.GetPointsLedgerUseCase;
import com.yowyob.loyalty.domain.loyalty.port.out.PointsTransactionRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetPointsLedgerHandler implements GetPointsLedgerUseCase {

    private final PointsTransactionRepository pointsTransactionRepo;

    public GetPointsLedgerHandler(PointsTransactionRepository pointsTransactionRepo) {
        this.pointsTransactionRepo = pointsTransactionRepo;
    }

    @Override
    public List<PointsTransaction> getTenantLedger(TenantId tenantId, int page, int size) {
        return pointsTransactionRepo.findByTenantId(tenantId, page, size);
    }
}
