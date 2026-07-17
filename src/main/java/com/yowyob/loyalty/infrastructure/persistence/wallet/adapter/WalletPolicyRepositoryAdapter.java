package com.yowyob.loyalty.infrastructure.persistence.wallet.adapter;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.wallet.model.WalletPolicy;
import com.yowyob.loyalty.domain.wallet.port.out.WalletPolicyRepository;
import com.yowyob.loyalty.infrastructure.persistence.wallet.mapper.WalletPolicyMapper;
import com.yowyob.loyalty.infrastructure.persistence.wallet.repository.WalletPolicyR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class WalletPolicyRepositoryAdapter implements WalletPolicyRepository {
    private final WalletPolicyR2dbcRepository r2dbcRepo;
    private final WalletPolicyMapper mapper;

    public WalletPolicyRepositoryAdapter(WalletPolicyR2dbcRepository r2dbcRepo, WalletPolicyMapper mapper) {
        this.r2dbcRepo = r2dbcRepo;
        this.mapper = mapper;
    }

    @Override
    public Mono<WalletPolicy> findByTenant(TenantId tenantId) {
        return r2dbcRepo.findByTenantId(tenantId.value())
            .map(mapper::toDomain)
            .defaultIfEmpty(WalletPolicy.defaults());
    }

    @Override
    public Mono<WalletPolicy> save(TenantId tenantId, WalletPolicy policy) {
        return r2dbcRepo.findByTenantId(tenantId.value())
            .map(existing -> {
                var entity = mapper.toEntity(policy);
                entity.setId(existing.getId());
                entity.setTenantId(existing.getTenantId());
                return entity;
            })
            .switchIfEmpty(Mono.fromSupplier(() -> {
                var entity = mapper.toEntity(policy);
                entity.setTenantId(tenantId.value());
                return entity;
            }))
            .flatMap(r2dbcRepo::save)
            .map(mapper::toDomain);
    }
}
