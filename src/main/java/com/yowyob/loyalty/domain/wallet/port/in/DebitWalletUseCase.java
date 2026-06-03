package com.yowyob.loyalty.domain.wallet.port.in;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.WalletDebitResult;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

public interface DebitWalletUseCase {
    Mono<WalletDebitResult> debit(TenantId tenantId, UserId memberId, BigDecimal amount, String description, String orderReference, String idempotencyKey);
}
