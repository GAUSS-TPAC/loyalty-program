package com.yowyob.loyaulty.program.domain.referral.port.out;

import com.yowyob.loyaulty.program.domain.referral.model.ReferralProgram;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

public interface ReferralProgramRepository {
    Mono<ReferralProgram> findByTenant(TenantId tenantId);
    Mono<ReferralProgram> save(ReferralProgram program);
}
