package com.yowyob.loyalty.application.loyalty.handler;

import com.yowyob.loyalty.domain.loyalty.model.points.PointsAccount;
import com.yowyob.loyalty.domain.loyalty.model.points.PointsTransaction;
import com.yowyob.loyalty.domain.loyalty.port.in.GetMemberPointsUseCase;
import com.yowyob.loyalty.domain.loyalty.port.out.PointsAccountRepository;
import com.yowyob.loyalty.domain.loyalty.port.out.PointsTransactionRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetMemberPointsHandler implements GetMemberPointsUseCase {

    private final PointsAccountRepository pointsAccountRepo;
    private final PointsTransactionRepository pointsTransactionRepo;

    public GetMemberPointsHandler(PointsAccountRepository pointsAccountRepo,
                                   PointsTransactionRepository pointsTransactionRepo) {
        this.pointsAccountRepo = pointsAccountRepo;
        this.pointsTransactionRepo = pointsTransactionRepo;
    }

    @Override
    public PointsAccount getPoints(TenantId tenantId, UserId memberId) {
        return pointsAccountRepo.findByMemberId(tenantId, memberId)
                .orElseGet(() -> PointsAccount.create(UUID.randomUUID(), tenantId, memberId));
    }

    @Override
    public List<PointsTransaction> getPointsHistory(TenantId tenantId, UserId memberId, int page, int size) {
        return pointsAccountRepo.findByMemberId(tenantId, memberId)
                .map(account -> pointsTransactionRepo.findByAccountId(account.getId(), size, page * size))
                .orElse(List.of());
    }
}
