package com.yowyob.loyaulty.program.domain.wallet.port.out;

import com.yowyob.loyaulty.program.domain.shared.model.PageRequest;
import com.yowyob.loyaulty.program.domain.shared.model.PageResult;
import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionType;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface WalletTransactionRepository {
    Mono<WalletTransaction> save(WalletTransaction transaction);
    Mono<WalletTransaction> findById(UUID id);
    Mono<PageResult<WalletTransaction>> findByWalletId(UUID walletId, PageRequest pageRequest);
    Mono<BigDecimal> sumDebitedToday(UUID walletId, Instant dayStart);
    Mono<WalletTransaction> findByIdempotencyKey(String idempotencyKey);
}
