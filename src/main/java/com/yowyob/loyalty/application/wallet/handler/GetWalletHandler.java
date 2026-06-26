package com.yowyob.loyalty.application.wallet.handler;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.Wallet;
import com.yowyob.loyalty.domain.wallet.port.in.GetWalletUseCase;
import com.yowyob.loyalty.domain.wallet.service.WalletDomainService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class GetWalletHandler implements GetWalletUseCase {

    private final WalletDomainService domainService;

    public GetWalletHandler(WalletDomainService domainService) {
        this.domainService = domainService;
    }

    @Override
    public Mono<Wallet> getWallet(TenantId tenantId, UserId memberId) {
        return domainService.getWallet(tenantId, memberId);
    }

    @Override
    public Mono<BigDecimal> getBalance(TenantId tenantId, UserId memberId) {
        return domainService.getBalance(tenantId, memberId);
    }
}
