package com.yowyob.loyalty.application.wallet.handler;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.Wallet;
import com.yowyob.loyalty.domain.wallet.port.in.FreezeWalletUseCase;
import com.yowyob.loyalty.domain.wallet.service.WalletDomainService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class FreezeWalletHandler implements FreezeWalletUseCase {

    private final WalletDomainService domainService;

    public FreezeWalletHandler(WalletDomainService domainService) {
        this.domainService = domainService;
    }

    @Override
    public Mono<Wallet> freeze(TenantId tenantId, UserId memberId, String reason, String actorId) {
        return domainService.freeze(tenantId, memberId, reason, actorId);
    }
}
