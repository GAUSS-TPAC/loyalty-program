package com.yowyob.loyaulty.program.infrastructure.persistence.reward.repository;

import com.yowyob.loyaulty.program.infrastructure.persistence.reward.entity.RewardGrantEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RewardGrantR2dbcRepository extends R2dbcRepository<RewardGrantEntity, UUID> {

    Mono<RewardGrantEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<RewardGrantEntity> findAllByMemberIdAndTenantIdAndStatus(String memberId,
                                                                   UUID tenantId,
                                                                   String status);

    Flux<RewardGrantEntity> findAllByMemberIdAndTenantId(String memberId, UUID tenantId);
}
