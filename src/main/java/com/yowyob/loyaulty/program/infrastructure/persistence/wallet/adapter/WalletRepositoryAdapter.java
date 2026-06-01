package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.adapter;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.model.Wallet;
import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletRepository;
import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.mapper.WalletMapper;
import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.repository.WalletR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class WalletRepositoryAdapter implements WalletRepository {

    private final WalletR2dbcRepository repository;
    private final WalletMapper mapper;

    public WalletRepositoryAdapter(WalletR2dbcRepository repository, WalletMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Wallet> findById(UUID walletId) {
        return repository.findById(walletId).map(mapper::toDomain);
    }

    @Override
    public Mono<Wallet> findByMemberAndTenant(String memberId, TenantId tenantId) {
        return repository.findByMemberIdAndTenantId(memberId, tenantId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Wallet> save(Wallet wallet) {
        return repository.save(mapper.toEntity(wallet)).map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByMemberAndTenant(String memberId, TenantId tenantId) {
        return repository.existsByMemberIdAndTenantId(memberId, tenantId.value());
    }
}
