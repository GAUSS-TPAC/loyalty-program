package com.yowyob.loyaulty.program.domain.wallet.port.in;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionSource;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface CreditWalletUseCase {
    Mono<WalletTransaction> credit(TenantId tenantId, String memberId,
                                    BigDecimal amount, TransactionSource source,
                                    String idempotencyKey);
}
