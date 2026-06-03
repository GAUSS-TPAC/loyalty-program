package com.yowyob.loyalty.application.wallet.handler;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.TransactionSource;
import com.yowyob.loyalty.domain.wallet.model.WalletCreditResult;
import com.yowyob.loyalty.domain.wallet.port.in.CreditWalletUseCase;
import com.yowyob.loyalty.domain.wallet.port.out.IdempotencyPort;
import com.yowyob.loyalty.domain.wallet.service.WalletDomainService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.Duration;

@Service
public class CreditWalletHandler implements CreditWalletUseCase {

    private final WalletDomainService domainService;
    private final IdempotencyPort idempotency;

    public CreditWalletHandler(WalletDomainService domainService, IdempotencyPort idempotency) {
        this.domainService = domainService;
        this.idempotency = idempotency;
    }

    @Override
    public Mono<WalletCreditResult> credit(TenantId tenantId, UserId memberId, BigDecimal amount, TransactionSource source, String referenceId, String idempotencyKey) {
        // En vrai ici on devrait sérialiser le résultat pour le cache d'idempotence
        // Pour faire simple et respecter le guide, on délègue au service
        return domainService.credit(tenantId, memberId, amount, source, referenceId, idempotencyKey);
    }
}
