package com.yowyob.loyalty.infrastructure.persistence.wallet.repository;

import com.yowyob.loyalty.infrastructure.persistence.wallet.entity.WalletTransactionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface WalletTransactionR2dbcRepository extends ReactiveCrudRepository<WalletTransactionEntity, UUID> {
    Mono<WalletTransactionEntity> findByIdempotencyKey(String key);
    Flux<WalletTransactionEntity> findByWalletIdOrderByCreatedAtDesc(UUID walletId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM wallet_transactions WHERE wallet_id = :walletId AND type IN ('DEBIT', 'RESERVE') AND status = 'COMPLETED' AND created_at >= :startOfDay")
    Mono<BigDecimal> sumDebitsTodayForWallet(UUID walletId, Instant startOfDay);
}
