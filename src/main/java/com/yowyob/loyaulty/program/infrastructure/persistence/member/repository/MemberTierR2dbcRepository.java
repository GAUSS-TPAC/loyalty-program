package com.yowyob.loyaulty.program.infrastructure.persistence.member.repository;

import com.yowyob.loyaulty.program.infrastructure.persistence.member.entity.MemberTierEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MemberTierR2dbcRepository extends R2dbcRepository<MemberTierEntity, UUID> {

    Mono<MemberTierEntity> findByMemberIdAndTenantId(String memberId, UUID tenantId);
}
