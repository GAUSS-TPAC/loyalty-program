package com.yowyob.loyaulty.program.domain.member.port.out;

import com.yowyob.loyaulty.program.domain.member.model.Member;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MemberRepository {

    Mono<Member> save(Member member);

    Mono<Member> findById(UUID id, TenantId tenantId);

    Mono<Member> findByExternalId(String externalId, TenantId tenantId);

    Mono<Boolean> existsByExternalId(String externalId, TenantId tenantId);

    Flux<Member> findAllByTenant(TenantId tenantId);
}
