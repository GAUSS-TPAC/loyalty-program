package com.yowyob.loyaulty.program.domain.member.port.in;

import com.yowyob.loyaulty.program.domain.member.model.Member;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

public interface EnrollMemberUseCase {

    Mono<Member> enroll(TenantId tenantId, String externalId,
                        String email, String phone, String displayName);
}
