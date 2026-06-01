package com.yowyob.loyaulty.program.infrastructure.persistence.member.repository;

import com.yowyob.loyaulty.program.infrastructure.persistence.member.entity.MemberEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MemberR2dbcRepository extends R2dbcRepository<MemberEntity, UUID> {

    Mono<MemberEntity> findByExternalIdAndTenantId(String externalId, UUID tenantId);

    Mono<Boolean> existsByExternalIdAndTenantId(String externalId, UUID tenantId);

    Flux<MemberEntity> findAllByTenantId(UUID tenantId);

    Mono<MemberEntity> findByIdAndTenantId(UUID id, UUID tenantId);
}
