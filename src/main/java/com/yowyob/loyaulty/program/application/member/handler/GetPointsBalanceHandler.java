package com.yowyob.loyaulty.program.application.member.handler;

import com.yowyob.loyaulty.program.domain.member.model.PointsAccount;
import com.yowyob.loyaulty.program.domain.member.model.PointsTransaction;
import com.yowyob.loyaulty.program.domain.member.port.in.GetPointsBalanceUseCase;
import com.yowyob.loyaulty.program.domain.member.port.out.PointsAccountRepository;
import com.yowyob.loyaulty.program.domain.member.port.out.PointsTransactionRepository;
import com.yowyob.loyaulty.program.domain.shared.model.PageRequest;
import com.yowyob.loyaulty.program.domain.shared.model.PageResult;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.shared.exception.AppException;
import com.yowyob.loyaulty.program.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GetPointsBalanceHandler implements GetPointsBalanceUseCase {

    private final PointsAccountRepository accountRepository;
    private final PointsTransactionRepository txRepository;

    public GetPointsBalanceHandler(PointsAccountRepository accountRepository,
                                    PointsTransactionRepository txRepository) {
        this.accountRepository = accountRepository;
        this.txRepository = txRepository;
    }

    @Override
    public Mono<PointsAccount> getBalance(TenantId tenantId, String memberId) {
        return accountRepository.findByMemberId(memberId, tenantId)
                .switchIfEmpty(Mono.error(new PointsAccountNotFoundException(memberId)));
    }

    @Override
    public Mono<PageResult<PointsTransaction>> getHistory(TenantId tenantId, String memberId,
                                                           PageRequest pageRequest) {
        return txRepository.findByMemberId(memberId, tenantId, pageRequest);
    }

    static class PointsAccountNotFoundException extends AppException {
        PointsAccountNotFoundException(String memberId) {
            super(ErrorCode.MEMBER_NOT_FOUND, "Points account not found for member: " + memberId);
        }
    }
}
