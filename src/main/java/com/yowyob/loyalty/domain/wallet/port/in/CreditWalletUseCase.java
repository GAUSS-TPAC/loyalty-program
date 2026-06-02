package com.yowyob.loyalty.domain.wallet.port.in;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.TransactionSource;
import com.yowyob.loyalty.domain.wallet.model.WalletCreditResult;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

public interface CreditWalletUseCase {
    Mono<WalletCreditResult> credit(TenantId tenantId, UserId memberId, BigDecimal amount, TransactionSource source, String referenceId, String idempotencyKey);
}
