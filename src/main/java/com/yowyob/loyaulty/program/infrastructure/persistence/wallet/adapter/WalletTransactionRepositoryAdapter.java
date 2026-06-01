package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.adapter;

import com.yowyob.loyaulty.program.domain.shared.model.PageRequest;
import com.yowyob.loyaulty.program.domain.shared.model.PageResult;
import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;
import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletTransactionRepository;
import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.mapper.WalletTransactionMapper;
import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.repository.WalletTransactionR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class WalletTransactionRepositoryAdapter implements WalletTransactionRepository {

    private final WalletTransactionR2dbcRepository repository;
    private final WalletTransactionMapper mapper;

    public WalletTransactionRepositoryAdapter(WalletTransactionR2dbcRepository repository,
                                               WalletTransactionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<WalletTransaction> save(WalletTransaction tx) {
        return repository.save(mapper.toEntity(tx)).map(mapper::toDomain);
    }

    @Override
    public Mono<WalletTransaction> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<PageResult<WalletTransaction>> findByWalletId(UUID walletId, PageRequest pageRequest) {
        return Mono.zip(
                repository.findByWalletIdPaged(walletId, pageRequest.size(), pageRequest.offset())
                        .map(mapper::toDomain).collectList(),
                repository.countByWalletId(walletId)
        ).map(tuple -> PageResult.of(tuple.getT1(), tuple.getT2(),
                pageRequest.page(), pageRequest.size()));
    }

    @Override
    public Mono<BigDecimal> sumDebitedToday(UUID walletId, Instant dayStart) {
        return repository.sumDebitedSince(walletId, dayStart).defaultIfEmpty(BigDecimal.ZERO);
    }

    @Override
    public Mono<WalletTransaction> findByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey).map(mapper::toDomain);
    }
}
