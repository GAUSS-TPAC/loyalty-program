package com.yowyob.loyalty.domain.wallet.port.in;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.wallet.model.Wallet;
import reactor.core.publisher.Flux;

public interface ListWalletsUseCase {
    Flux<Wallet> listWallets(TenantId tenantId, int page, int size);
}
