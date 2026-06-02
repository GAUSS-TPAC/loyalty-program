package com.yowyob.loyalty.application.wallet.handler;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.WalletDebitResult;
import com.yowyob.loyalty.domain.wallet.port.in.DebitWalletUseCase;
import com.yowyob.loyalty.domain.wallet.port.out.IdempotencyPort;
import com.yowyob.loyalty.domain.wallet.service.WalletDomainService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

@Service
public class DebitWalletHandler implements DebitWalletUseCase {

    private final WalletDomainService domainService;
    private final IdempotencyPort idempotency;

    public DebitWalletHandler(WalletDomainService domainService, IdempotencyPort idempotency) {
        this.domainService = domainService;
        this.idempotency = idempotency;
    }

    @Override
    public Mono<WalletDebitResult> debit(TenantId tenantId, UserId memberId, BigDecimal amount, String description, String orderReference, String idempotencyKey) {
        return domainService.debit(tenantId, memberId, amount, description, orderReference, idempotencyKey)
            .flatMap(result -> {
                if (result.otpRequired()) {
                    // TODO: Persister OTP challenge en Redis avec TTL 5 min
                }
                return Mono.just(result);
            });
    }
}
