package com.yowyob.loyalty.application.wallet.handler;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.wallet.model.WalletTransaction;
import com.yowyob.loyalty.domain.wallet.port.in.ReverseTransactionUseCase;
import com.yowyob.loyalty.domain.wallet.service.WalletDomainService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ReverseTransactionHandler implements ReverseTransactionUseCase {

    private final WalletDomainService domainService;

    public ReverseTransactionHandler(WalletDomainService domainService) {
        this.domainService = domainService;
    }

    @Override
    public Mono<WalletTransaction> reverse(TenantId tenantId, UUID transactionId, String reason, String actorId, String idempotencyKey) {
        return domainService.reverse(tenantId, transactionId, reason, actorId, idempotencyKey);
    }
}
