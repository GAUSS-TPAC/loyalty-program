package com.yowyob.loyalty.infrastructure.persistence.wallet.adapter;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.Wallet;
import com.yowyob.loyalty.domain.wallet.port.out.WalletRepository;
import com.yowyob.loyalty.infrastructure.persistence.wallet.entity.WalletEntity;
import com.yowyob.loyalty.infrastructure.persistence.wallet.mapper.WalletMapper;
import com.yowyob.loyalty.infrastructure.persistence.wallet.repository.WalletR2dbcRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Component
public class WalletRepositoryAdapter implements WalletRepository {
    private final WalletR2dbcRepository r2dbcRepo;
    private final WalletMapper mapper;
    private final R2dbcEntityTemplate template;

    public WalletRepositoryAdapter(WalletR2dbcRepository r2dbcRepo, WalletMapper mapper, R2dbcEntityTemplate template) {
        this.r2dbcRepo = r2dbcRepo;
        this.mapper = mapper;
        this.template = template;
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

    // Client-generated UUID id + no Persistable => save() always issues UPDATE. Decide
    // insert vs update explicitly (see RuleRepositoryAdapter for the full explanation).
    @Override
    public Mono<Wallet> save(Wallet wallet) {
        WalletEntity entity = mapper.toEntity(wallet);
        return r2dbcRepo.existsById(entity.getId())
                .flatMap(exists -> exists ? r2dbcRepo.save(entity) : template.insert(entity))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByMemberAndTenant(UserId memberId, TenantId tenantId) {
        return r2dbcRepo.existsByMemberIdAndTenantId(memberId.value(), tenantId.value());
    }

    @Override
    public Flux<Wallet> findAllByTenant(TenantId tenantId, int page, int size) {
        return r2dbcRepo.findByTenantIdOrderByCreatedAtDesc(tenantId.value(), PageRequest.of(page, size))
                .map(mapper::toDomain);
    }
}
