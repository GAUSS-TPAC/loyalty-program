package com.yowyob.loyaulty.program.domain.wallet.port.in;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReverseTransactionUseCase {
    Mono<WalletTransaction> reverse(TenantId tenantId, String memberId, UUID transactionId, String idempotencyKey);
}
