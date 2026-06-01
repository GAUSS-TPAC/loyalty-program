package com.yowyob.loyaulty.program.infrastructure.persistence.reward.adapter;

import com.yowyob.loyaulty.program.domain.reward.model.Reward;
import com.yowyob.loyaulty.program.domain.reward.port.out.RewardRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.infrastructure.persistence.reward.mapper.RewardMapper;
import com.yowyob.loyaulty.program.infrastructure.persistence.reward.repository.RewardR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RewardRepositoryAdapter implements RewardRepository {

    private final RewardR2dbcRepository repository;
    private final RewardMapper mapper;

    public RewardRepositoryAdapter(RewardR2dbcRepository repository, RewardMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Reward> save(Reward reward) {
        return repository.save(mapper.toEntity(reward)).map(mapper::toDomain);
    }

    @Override
    public Mono<Reward> findById(UUID id, TenantId tenantId) {
        return repository.findByIdAndTenantId(id, tenantId.value()).map(mapper::toDomain);
    }

    @Override
    public Flux<Reward> findAllByTenant(TenantId tenantId) {
        return repository.findAllByTenantId(tenantId.value()).map(mapper::toDomain);
    }

    @Override
    public Flux<Reward> findActiveByTenant(TenantId tenantId) {
        return repository.findAllByTenantIdAndStatus(tenantId.value(), "ACTIVE")
                .map(mapper::toDomain);
    }
}
