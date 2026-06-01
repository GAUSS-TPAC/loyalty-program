package com.yowyob.loyaulty.program.domain.wallet.port.in;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.model.Wallet;
import reactor.core.publisher.Mono;

public interface GetWalletBalanceUseCase {
    Mono<Wallet> getWallet(TenantId tenantId, String memberId);
}
