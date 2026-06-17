package com.yowyob.loyalty.application.wallet.handler;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.Wallet;
import com.yowyob.loyalty.domain.wallet.port.in.UnfreezeWalletUseCase;
import com.yowyob.loyalty.domain.wallet.service.WalletDomainService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UnfreezeWalletHandler implements UnfreezeWalletUseCase {

    private final WalletDomainService domainService;

    public UnfreezeWalletHandler(WalletDomainService domainService) {
        this.domainService = domainService;
    }

    @Override
    public Mono<Wallet> unfreeze(TenantId tenantId, UserId memberId, String actorId) {
        return domainService.unfreeze(tenantId, memberId, actorId);
    }
}
