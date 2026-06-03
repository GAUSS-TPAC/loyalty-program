package com.yowyob.loyalty.domain.wallet.port.in;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.Wallet;
import reactor.core.publisher.Mono;

public interface UnfreezeWalletUseCase {
    Mono<Wallet> unfreeze(TenantId tenantId, UserId memberId, String actorId);
}
