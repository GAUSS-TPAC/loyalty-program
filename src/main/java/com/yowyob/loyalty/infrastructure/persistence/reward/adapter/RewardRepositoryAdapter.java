package com.yowyob.loyalty.infrastructure.persistence.reward.adapter;

import com.yowyob.loyalty.domain.reward.exception.RewardDomainException;
import com.yowyob.loyalty.domain.reward.exception.RewardNotFoundException;
import com.yowyob.loyalty.domain.reward.model.Reward;
import com.yowyob.loyalty.domain.reward.port.out.RewardRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.persistence.reward.entity.RewardEntity;
import com.yowyob.loyalty.infrastructure.persistence.reward.mapper.RewardMapper;
import com.yowyob.loyalty.infrastructure.persistence.reward.repository.RewardR2dbcRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RewardRepositoryAdapter implements RewardRepository {

    private final RewardR2dbcRepository r2dbcRepo;
    private final RewardMapper mapper;
    private final R2dbcEntityTemplate template;

    public RewardRepositoryAdapter(RewardR2dbcRepository r2dbcRepo, RewardMapper mapper, R2dbcEntityTemplate template) {
        this.r2dbcRepo = r2dbcRepo;
        this.mapper = mapper;
        this.template = template;
    }

    // Client-generated UUID id + no Persistable => save() always issues UPDATE. Decide
    // insert vs update explicitly (see RuleRepositoryAdapter for the full explanation).
    @Override
    public Mono<Reward> save(Reward reward) {
        RewardEntity entity = mapper.toEntity(reward);
        return r2dbcRepo.existsById(entity.getId())
                .flatMap(exists -> exists ? r2dbcRepo.save(entity) : template.insert(entity))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Reward> findById(UUID id) {
        return r2dbcRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<Reward> findByIdAndTenant(UUID id, TenantId tenantId) {
        return r2dbcRepo.findByIdAndTenantId(id, tenantId.value())
                .map(mapper::toDomain)
                .switchIfEmpty(Mono.error(new RewardNotFoundException(id)));
    }

    @Override
    public Flux<Reward> findByTenant(TenantId tenantId, boolean activeOnly, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        if (activeOnly) {
            return r2dbcRepo.findByTenantIdAndStatus(tenantId.value(), "ACTIVE", pageable).map(mapper::toDomain);
        }
        return r2dbcRepo.findByTenantId(tenantId.value(), pageable).map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByIdAndTenant(UUID id, TenantId tenantId) {
        return r2dbcRepo.existsByIdAndTenantId(id, tenantId.value());
    }

    public Mono<Void> decrementStockAtomically(UUID id, TenantId tenantId, int version) {
        return r2dbcRepo.decrementStockAtomically(id, tenantId.value(), version)
                .flatMap(rowsAffected -> rowsAffected == 0
                        ? Mono.error(new RewardDomainException("Stock épuisé ou conflit de version"))
                        : Mono.empty());
    }
}
