package com.yowyob.loyaulty.program.infrastructure.persistence.member.repository;

import com.yowyob.loyaulty.program.infrastructure.persistence.member.entity.PointsAccountEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PointsAccountR2dbcRepository extends R2dbcRepository<PointsAccountEntity, UUID> {

    Mono<PointsAccountEntity> findByMemberIdAndTenantId(String memberId, UUID tenantId);
}
