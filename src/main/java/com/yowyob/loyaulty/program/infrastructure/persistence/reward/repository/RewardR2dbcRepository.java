package com.yowyob.loyaulty.program.infrastructure.persistence.reward.repository;

import com.yowyob.loyaulty.program.infrastructure.persistence.reward.entity.RewardEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RewardR2dbcRepository extends R2dbcRepository<RewardEntity, UUID> {

    Flux<RewardEntity> findAllByTenantId(UUID tenantId);

    Flux<RewardEntity> findAllByTenantIdAndStatus(UUID tenantId, String status);

    Mono<RewardEntity> findByIdAndTenantId(UUID id, UUID tenantId);
}
