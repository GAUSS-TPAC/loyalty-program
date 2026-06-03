package com.yowyob.loyalty.domain.wallet.port.in;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.Wallet;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

public interface GetWalletUseCase {
    Mono<Wallet> getWallet(TenantId tenantId, UserId memberId);
    Mono<BigDecimal> getBalance(TenantId tenantId, UserId memberId);
}
