package com.yowyob.loyaulty.program.domain.wallet.port.in;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

public interface CloseWalletUseCase {
    Mono<Void> close(TenantId tenantId, String memberId);
}
