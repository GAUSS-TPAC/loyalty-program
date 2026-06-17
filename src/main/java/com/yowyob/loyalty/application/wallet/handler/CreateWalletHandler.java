package com.yowyob.loyalty.application.wallet.handler;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.Wallet;
import com.yowyob.loyalty.domain.wallet.port.in.CreateWalletUseCase;
import com.yowyob.loyalty.domain.wallet.service.WalletDomainService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CreateWalletHandler implements CreateWalletUseCase {

    private final WalletDomainService domainService;

    public CreateWalletHandler(WalletDomainService domainService) {
        this.domainService = domainService;
    }

    @Override
    public Mono<Wallet> createWallet(TenantId tenantId, UserId memberId, String currencyCode, boolean autoActivate, String idempotencyKey) {
        return domainService.createWallet(tenantId, memberId, currencyCode, autoActivate, idempotencyKey);
    }
}
