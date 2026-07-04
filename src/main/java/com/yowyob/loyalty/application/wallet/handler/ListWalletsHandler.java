package com.yowyob.loyalty.application.wallet.handler;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.wallet.model.Wallet;
import com.yowyob.loyalty.domain.wallet.port.in.ListWalletsUseCase;
import com.yowyob.loyalty.domain.wallet.service.WalletDomainService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ListWalletsHandler implements ListWalletsUseCase {

    private final WalletDomainService domainService;

    public ListWalletsHandler(WalletDomainService domainService) {
        this.domainService = domainService;
    }

    @Override
    public Flux<Wallet> listWallets(TenantId tenantId, int page, int size) {
        return domainService.listWallets(tenantId, page, size);
    }
}
