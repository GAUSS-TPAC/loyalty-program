package com.yowyob.loyaulty.program.application.wallet.handler;

import com.yowyob.loyaulty.program.domain.shared.model.PageRequest;
import com.yowyob.loyaulty.program.domain.shared.model.PageResult;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;
import com.yowyob.loyaulty.program.domain.wallet.port.in.GetTransactionHistoryUseCase;
import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletRepository;
import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletTransactionRepository;
import com.yowyob.loyaulty.program.shared.exception.WalletNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GetTransactionHistoryHandler implements GetTransactionHistoryUseCase {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public GetTransactionHistoryHandler(WalletRepository walletRepository,
                                         WalletTransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Mono<PageResult<WalletTransaction>> getHistory(TenantId tenantId, String memberId,
                                                           PageRequest pageRequest) {
        return walletRepository.findByMemberAndTenant(memberId, tenantId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException(memberId)))
                .flatMap(wallet -> transactionRepository.findByWalletId(wallet.getId(), pageRequest));
    }
}
