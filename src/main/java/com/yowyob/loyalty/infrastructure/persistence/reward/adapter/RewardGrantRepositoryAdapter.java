package com.yowyob.loyalty.infrastructure.persistence.reward.adapter;

import com.yowyob.loyalty.domain.reward.exception.GrantNotFoundException;
import com.yowyob.loyalty.domain.reward.model.RewardGrant;
import com.yowyob.loyalty.domain.reward.port.out.RewardGrantRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.infrastructure.persistence.reward.entity.RewardGrantEntity;
import com.yowyob.loyalty.infrastructure.persistence.reward.mapper.RewardGrantMapper;
import com.yowyob.loyalty.infrastructure.persistence.reward.repository.RewardGrantR2dbcRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Component
public class RewardGrantRepositoryAdapter implements RewardGrantRepository {

    private final RewardGrantR2dbcRepository r2dbcRepo;
    private final RewardGrantMapper mapper;
    private final R2dbcEntityTemplate template;

    public RewardGrantRepositoryAdapter(RewardGrantR2dbcRepository r2dbcRepo, RewardGrantMapper mapper, R2dbcEntityTemplate template) {
        this.r2dbcRepo = r2dbcRepo;
        this.mapper = mapper;
        this.template = template;
    }

    // Client-generated UUID id + no Persistable => save() always issues UPDATE. Decide
    // insert vs update explicitly (see RuleRepositoryAdapter for the full explanation).
    @Override
    public Mono<RewardGrant> save(RewardGrant grant) {
        RewardGrantEntity entity = mapper.toEntity(grant);
        return r2dbcRepo.existsById(entity.getId())
                .flatMap(exists -> exists ? r2dbcRepo.save(entity) : template.insert(entity))
                .map(mapper::toDomain);
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
