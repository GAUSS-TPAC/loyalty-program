package com.yowyob.loyalty.domain.wallet.port.out;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.wallet.model.TransactionSource;
import com.yowyob.loyalty.domain.wallet.model.TransactionType;
import com.yowyob.loyalty.domain.wallet.model.WalletTransaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface WalletTransactionRepository {
    Mono<WalletTransaction> save(WalletTransaction transaction);
    Mono<WalletTransaction> findById(UUID id);
    Mono<WalletTransaction> findByIdempotencyKey(String key);
    Flux<WalletTransaction> findByWalletId(UUID walletId, int page, int size);
    Flux<WalletTransaction> findByWalletIdAndFilters(UUID walletId, TransactionType type, TransactionSource source, Instant from, Instant to, int page, int size);
    Mono<BigDecimal> sumDebitsTodayForWallet(UUID walletId);
}
