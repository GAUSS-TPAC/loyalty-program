package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.repository;

import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.entity.WalletTransactionEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface WalletTransactionR2dbcRepository
        extends ReactiveCrudRepository<WalletTransactionEntity, UUID> {

    Flux<WalletTransactionEntity> findByWalletIdOrderByCreatedAtDesc(UUID walletId);

    Mono<WalletTransactionEntity> findByIdempotencyKey(String idempotencyKey);

    @Query("""
        SELECT COALESCE(SUM(amount), 0)
        FROM wallet_transactions
        WHERE wallet_id = :walletId
          AND type = 'DEBIT'
          AND status = 'COMPLETED'
          AND created_at >= :dayStart
        """)
    Mono<BigDecimal> sumDebitedSince(UUID walletId, Instant dayStart);

    @Query("""
        SELECT * FROM wallet_transactions
        WHERE wallet_id = :walletId
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
        """)
    Flux<WalletTransactionEntity> findByWalletIdPaged(UUID walletId, int limit, int offset);

    @Query("SELECT COUNT(*) FROM wallet_transactions WHERE wallet_id = :walletId")
    Mono<Long> countByWalletId(UUID walletId);
}
