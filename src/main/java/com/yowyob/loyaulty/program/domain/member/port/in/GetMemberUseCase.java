package com.yowyob.loyaulty.program.domain.member.port.in;

import com.yowyob.loyaulty.program.domain.member.model.Member;
import com.yowyob.loyaulty.program.domain.member.model.MemberTier;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetMemberUseCase {

    Mono<Member> getById(TenantId tenantId, UUID memberId);

    Mono<Member> getByExternalId(TenantId tenantId, String externalId);

    Flux<Member> listAll(TenantId tenantId);

    Mono<MemberTier> getTier(TenantId tenantId, String memberId);
}
