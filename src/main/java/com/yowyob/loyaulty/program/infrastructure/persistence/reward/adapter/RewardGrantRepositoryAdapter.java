package com.yowyob.loyaulty.program.infrastructure.persistence.reward.adapter;

import com.yowyob.loyaulty.program.domain.reward.model.RewardGrant;
import com.yowyob.loyaulty.program.domain.reward.port.out.RewardGrantRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.infrastructure.persistence.reward.mapper.RewardMapper;
import com.yowyob.loyaulty.program.infrastructure.persistence.reward.repository.RewardGrantR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RewardGrantRepositoryAdapter implements RewardGrantRepository {

    private final RewardGrantR2dbcRepository repository;
    private final RewardMapper mapper;

    public RewardGrantRepositoryAdapter(RewardGrantR2dbcRepository repository, RewardMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<RewardGrant> save(RewardGrant grant) {
        return repository.save(mapper.toEntity(grant)).map(mapper::toDomain);
    }

    @Override
    public Mono<RewardGrant> findById(UUID id, TenantId tenantId) {
        return repository.findByIdAndTenantId(id, tenantId.value()).map(mapper::toDomain);
    }

    @Override
    public Flux<RewardGrant> findActiveByMember(String memberId, TenantId tenantId) {
        return repository.findAllByMemberIdAndTenantIdAndStatus(memberId, tenantId.value(), "ACTIVE")
                .map(mapper::toDomain);
    }

    @Override
    public Flux<RewardGrant> findAllByMember(String memberId, TenantId tenantId) {
        return repository.findAllByMemberIdAndTenantId(memberId, tenantId.value())
                .map(mapper::toDomain);
    }
}
