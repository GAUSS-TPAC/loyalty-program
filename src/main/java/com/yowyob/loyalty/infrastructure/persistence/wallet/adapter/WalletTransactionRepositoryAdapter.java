package com.yowyob.loyalty.infrastructure.persistence.wallet.adapter;

import com.yowyob.loyalty.domain.wallet.model.TransactionSource;
import com.yowyob.loyalty.domain.wallet.model.TransactionType;
import com.yowyob.loyalty.domain.wallet.model.WalletTransaction;
import com.yowyob.loyalty.domain.wallet.port.out.WalletTransactionRepository;
import com.yowyob.loyalty.infrastructure.persistence.wallet.mapper.WalletTransactionMapper;
import com.yowyob.loyalty.infrastructure.persistence.wallet.repository.WalletTransactionR2dbcRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class WalletTransactionRepositoryAdapter implements WalletTransactionRepository {

    private final WalletTransactionR2dbcRepository repository;
    private final WalletTransactionMapper mapper;

    public WalletTransactionRepositoryAdapter(
            WalletTransactionR2dbcRepository repository,
            WalletTransactionMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<WalletTransaction> save(WalletTransaction transaction) {
        return repository.save(mapper.toEntity(transaction)).map(mapper::toDomain);
    }

    @Override
    public Mono<WalletTransaction> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<WalletTransaction> findByIdempotencyKey(String key) {
        return repository.findByIdempotencyKey(key).map(mapper::toDomain);
    }

    @Override
    public Flux<WalletTransaction> findByWalletId(UUID walletId, int page, int size) {
        return repository.findByWalletIdOrderByCreatedAtDesc(walletId, PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<WalletTransaction> findByWalletIdAndFilters(
            UUID walletId,
            TransactionType type,
            TransactionSource source,
            Instant from,
            Instant to,
            int page,
            int size
    ) {
        return findByWalletId(walletId, page, size)
                .filter(tx -> type == null || tx.type() == type)
                .filter(tx -> source == null || tx.source() == source)
                .filter(tx -> from == null || !tx.createdAt().isBefore(from))
                .filter(tx -> to == null || !tx.createdAt().isAfter(to));
    }

    @Override
    public Mono<BigDecimal> sumDebitsTodayForWallet(UUID walletId) {
        Instant startOfDay = Instant.now().atZone(ZoneOffset.UTC).toLocalDate()
                .atStartOfDay(ZoneOffset.UTC).toInstant();
        return repository.sumDebitsTodayForWallet(walletId, startOfDay);
    }
}
