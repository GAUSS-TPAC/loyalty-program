package com.yowyob.loyalty.domain.wallet.port.in;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.wallet.model.WalletTransaction;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface ReverseTransactionUseCase {
    Mono<WalletTransaction> reverse(TenantId tenantId, UUID transactionId, String reason, String actorId, String idempotencyKey);
}
