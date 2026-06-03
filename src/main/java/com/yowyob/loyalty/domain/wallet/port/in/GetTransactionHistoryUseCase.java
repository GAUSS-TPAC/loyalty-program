package com.yowyob.loyalty.domain.wallet.port.in;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.TransactionSource;
import com.yowyob.loyalty.domain.wallet.model.TransactionType;
import com.yowyob.loyalty.domain.wallet.model.WalletTransaction;
import reactor.core.publisher.Flux;
import java.time.Instant;

public interface GetTransactionHistoryUseCase {
    Flux<WalletTransaction> getHistory(TenantId tenantId, UserId memberId, TransactionType typeFilter, TransactionSource sourceFilter, Instant from, Instant to, int page, int size);
}
