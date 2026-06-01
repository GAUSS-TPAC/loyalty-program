package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.adapter;

import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionSource;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionStatus;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionType;
import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletTransactionRepository;
import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.mapper.WalletTransactionMapper;
import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.repository.WalletTransactionR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Adaptateur infrastructure implémentant {@link WalletTransactionRepository}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WalletTransactionRepositoryAdapter implements WalletTransactionRepository {

    private final WalletTransactionR2dbcRepository r2dbcRepository;
    private final WalletTransactionMapper mapper;

    @Override
    public Mono<WalletTransaction> findById(UUID transactionId, UUID tenantId) {
        return r2dbcRepository.findByIdAndTenantId(transactionId, tenantId)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<WalletTransaction> findByIdempotencyKey(String idempotencyKey, UUID tenantId) {
        return r2dbcRepository.findByIdempotencyKeyAndTenantId(idempotencyKey, tenantId)
                .map(mapper::toDomain)
                .doOnNext(t -> log.debug("Transaction idempotente trouvée : key={}", idempotencyKey));
    }

    @Override
    public Mono<WalletTransaction> save(WalletTransaction transaction) {
        return r2dbcRepository.save(mapper.toEntity(transaction))
                .map(mapper::toDomain)
                .doOnNext(t -> log.debug("Transaction créée : id={}, type={}, amount={}",
                        t.getId(), t.getType(), t.getAmount()));
    }

    @Override
    public Mono<WalletTransaction> updateStatus(UUID transactionId, UUID tenantId,
                                                 TransactionStatus newStatus, Instant completedAt) {
        return r2dbcRepository
                .updateStatus(transactionId, tenantId, newStatus.name(), completedAt)
                .flatMap(rows -> {
                    if (rows == 0) {
                        return Mono.error(new IllegalStateException(
                                "Transaction introuvable pour mise à jour : id=" + transactionId));
                    }
                    return findById(transactionId, tenantId);
                });
    }

    @Override
    public Flux<WalletTransaction> findByWalletId(UUID walletId, UUID tenantId,
                                                   TransactionType type,
                                                   TransactionSource source,
                                                   TransactionStatus status,
                                                   Instant dateFrom, Instant dateTo,
                                                   long offset, int limit) {
        return r2dbcRepository.findByFilters(
                        walletId, tenantId,
                        type   != null ? type.name()   : null,
                        source != null ? source.name() : null,
                        status != null ? status.name() : null,
                        dateFrom, dateTo,
                        limit, offset
                )
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countByWalletId(UUID walletId, UUID tenantId,
                                       TransactionType type,
                                       TransactionSource source,
                                       TransactionStatus status,
                                       Instant dateFrom, Instant dateTo) {
        return r2dbcRepository.countByFilters(
                walletId, tenantId,
                type   != null ? type.name()   : null,
                source != null ? source.name() : null,
                status != null ? status.name() : null,
                dateFrom, dateTo
        );
    }

    @Override
    public Mono<BigDecimal> sumDebitsSince(UUID walletId, UUID tenantId, Instant since) {
        return r2dbcRepository.sumCompletedDebitsSince(walletId, tenantId, since)
                .defaultIfEmpty(BigDecimal.ZERO);
    }
}
