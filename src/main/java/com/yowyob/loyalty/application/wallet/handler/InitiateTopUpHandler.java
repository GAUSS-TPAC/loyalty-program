package com.yowyob.loyalty.application.wallet.handler;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.PaymentInitiationResult;
import com.yowyob.loyalty.domain.wallet.port.in.InitiateTopUpUseCase;
import com.yowyob.loyalty.domain.wallet.port.out.IdempotencyPort;
import com.yowyob.loyalty.domain.wallet.service.WalletDomainService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

@Service
public class InitiateTopUpHandler implements InitiateTopUpUseCase {

    private final WalletDomainService domainService;
    private final IdempotencyPort idempotency;

    public InitiateTopUpHandler(WalletDomainService domainService, IdempotencyPort idempotency) {
        this.domainService = domainService;
        this.idempotency = idempotency;
    }

    @Override
    public Mono<PaymentInitiationResult> initiateTopUp(TenantId tenantId, UserId memberId, BigDecimal amount, String provider, String idempotencyKey) {
        // En vrai: charger policy, valider, créer PaymentRequest, appeler gateway
        // Ici on simplifie pour respecter le cadre
        return Mono.error(new UnsupportedOperationException("TopUp implementation required"));
    }
}
