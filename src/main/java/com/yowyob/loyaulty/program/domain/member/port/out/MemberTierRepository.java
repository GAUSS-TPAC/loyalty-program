package com.yowyob.loyaulty.program.domain.member.port.out;

import com.yowyob.loyaulty.program.domain.member.model.MemberTier;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

public interface MemberTierRepository {

    Mono<MemberTier> save(MemberTier tier);

    Mono<MemberTier> findByMemberId(String memberId, TenantId tenantId);
}
