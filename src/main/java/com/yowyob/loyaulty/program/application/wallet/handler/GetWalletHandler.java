package com.yowyob.loyaulty.program.application.wallet.handler;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.model.Wallet;
import com.yowyob.loyaulty.program.domain.wallet.port.in.GetWalletBalanceUseCase;
import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletRepository;
import com.yowyob.loyaulty.program.shared.exception.WalletNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GetWalletHandler implements GetWalletBalanceUseCase {

    private final WalletRepository walletRepository;

    public GetWalletHandler(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public Mono<Wallet> getWallet(TenantId tenantId, String memberId) {
        return walletRepository.findByMemberAndTenant(memberId, tenantId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException(memberId)));
    }
}
