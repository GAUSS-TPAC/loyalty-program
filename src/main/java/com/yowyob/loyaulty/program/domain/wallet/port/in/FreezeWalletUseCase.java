package com.yowyob.loyaulty.program.domain.wallet.port.in;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

public interface FreezeWalletUseCase {
    Mono<Void> freeze(TenantId tenantId, String memberId, String reason);
    Mono<Void> unfreeze(TenantId tenantId, String memberId);
}
