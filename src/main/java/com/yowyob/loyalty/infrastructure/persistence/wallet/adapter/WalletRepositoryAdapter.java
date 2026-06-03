package com.yowyob.loyalty.infrastructure.persistence.wallet.adapter;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.Wallet;
import com.yowyob.loyalty.domain.wallet.port.out.WalletRepository;
import com.yowyob.loyalty.infrastructure.persistence.wallet.mapper.WalletMapper;
import com.yowyob.loyalty.infrastructure.persistence.wallet.repository.WalletR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Component
public class WalletRepositoryAdapter implements WalletRepository {
    private final WalletR2dbcRepository r2dbcRepo;
    private final WalletMapper mapper;

    public WalletRepositoryAdapter(WalletR2dbcRepository r2dbcRepo, WalletMapper mapper) {
        this.r2dbcRepo = r2dbcRepo;
        this.mapper = mapper;
    }

    @Override
    public Mono<Wallet> findByMemberAndTenant(UserId memberId, TenantId tenantId) {
        return r2dbcRepo.findByMemberIdAndTenantId(memberId.value(), tenantId.value())
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Wallet> findById(UUID id) {
        return r2dbcRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<Wallet> save(Wallet wallet) {
        return r2dbcRepo.save(mapper.toEntity(wallet)).map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByMemberAndTenant(UserId memberId, TenantId tenantId) {
        return r2dbcRepo.existsByMemberIdAndTenantId(memberId.value(), tenantId.value());
    }
}
