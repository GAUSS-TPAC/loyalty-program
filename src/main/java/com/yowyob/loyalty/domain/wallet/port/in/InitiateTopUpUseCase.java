package com.yowyob.loyalty.domain.wallet.port.in;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.PaymentInitiationResult;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

public interface InitiateTopUpUseCase {
    Mono<PaymentInitiationResult> initiateTopUp(TenantId tenantId, UserId memberId, BigDecimal amount, String provider, String idempotencyKey);
}
