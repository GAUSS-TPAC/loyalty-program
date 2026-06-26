package com.yowyob.loyalty.infrastructure.persistence.reward.adapter;

import com.yowyob.loyalty.domain.reward.exception.GrantNotFoundException;
import com.yowyob.loyalty.domain.reward.model.RewardGrant;
import com.yowyob.loyalty.domain.reward.port.out.RewardGrantRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.infrastructure.persistence.reward.mapper.RewardGrantMapper;
import com.yowyob.loyalty.infrastructure.persistence.reward.repository.RewardGrantR2dbcRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Component
public class RewardGrantRepositoryAdapter implements RewardGrantRepository {

    private final RewardGrantR2dbcRepository r2dbcRepo;
    private final RewardGrantMapper mapper;

    public RewardGrantRepositoryAdapter(RewardGrantR2dbcRepository r2dbcRepo, RewardGrantMapper mapper) {
        this.r2dbcRepo = r2dbcRepo;
        this.mapper = mapper;
    }

    @Override
    public Mono<RewardGrant> save(RewardGrant grant) {
        return r2dbcRepo.save(mapper.toEntity(grant)).map(mapper::toDomain);
    }

    @Override
    public Mono<RewardGrant> findById(UUID id) {
        return r2dbcRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<RewardGrant> findByIdAndTenant(UUID id, TenantId tenantId) {
        return r2dbcRepo.findByIdAndTenantId(id, tenantId.value())
                .map(mapper::toDomain)
                .switchIfEmpty(Mono.error(new GrantNotFoundException(id)));
    }

    @Override
    public Mono<RewardGrant> findByIdempotencyKey(String key) {
        return r2dbcRepo.findByIdempotencyKey(key).map(mapper::toDomain);
    }

    @Override
    public Flux<RewardGrant> findActiveByMember(UserId memberId, TenantId tenantId) {
        return r2dbcRepo.findByMemberIdAndTenantIdAndStatus(memberId.value(), tenantId.value(), "ACTIVE")
                .map(mapper::toDomain);
    }

    @Override
    public Flux<RewardGrant> findAllByMember(UserId memberId, TenantId tenantId, int page, int size) {
        return r2dbcRepo.findByMemberIdAndTenantId(memberId.value(), tenantId.value(), PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<RewardGrant> findExpiredActive(Instant before) {
        return r2dbcRepo.findExpiredActiveGrants(before).map(mapper::toDomain);
    }
}
