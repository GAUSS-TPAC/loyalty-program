package com.yowyob.loyalty.application.loyalty.handler;

import com.yowyob.loyalty.domain.loyalty.model.points.ApiKeyPointsFlow;
import com.yowyob.loyalty.domain.loyalty.model.points.PointsAccount;
import com.yowyob.loyalty.domain.loyalty.model.points.PointsLedgerEntry;
import com.yowyob.loyalty.domain.loyalty.model.points.PointsTransaction;
import com.yowyob.loyalty.domain.loyalty.port.in.GetPointsLedgerUseCase;
import com.yowyob.loyalty.domain.loyalty.port.out.PointsAccountRepository;
import com.yowyob.loyalty.domain.loyalty.port.out.PointsTransactionRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GetPointsLedgerHandler implements GetPointsLedgerUseCase {

    private final PointsTransactionRepository pointsTransactionRepo;
    private final PointsAccountRepository pointsAccountRepo;

    public GetPointsLedgerHandler(PointsTransactionRepository pointsTransactionRepo,
                                   PointsAccountRepository pointsAccountRepo) {
        this.pointsTransactionRepo = pointsTransactionRepo;
        this.pointsAccountRepo = pointsAccountRepo;
    }

    @Override
    public List<PointsLedgerEntry> getTenantLedger(TenantId tenantId, int page, int size) {
        List<PointsTransaction> transactions = pointsTransactionRepo.findByTenantId(tenantId, page, size);
        Map<UUID, UserId> memberIdByAccount = new HashMap<>();

        return transactions.stream()
                .map(tx -> new PointsLedgerEntry(tx, memberIdByAccount.computeIfAbsent(tx.pointsAccountId(),
                        accountId -> pointsAccountRepo.findById(accountId)
                                .map(PointsAccount::getMemberId)
                                .orElse(null))))
                .toList();
    }

    @Override
    public List<ApiKeyPointsFlow> getFlowByApiKey(TenantId tenantId) {
        return pointsTransactionRepo.aggregateFlowByApiKey(tenantId);
    }
}
