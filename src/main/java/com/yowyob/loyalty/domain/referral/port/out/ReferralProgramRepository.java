package com.yowyob.loyalty.domain.referral.port.out;

import com.yowyob.loyalty.domain.referral.model.ReferralProgram;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReferralProgramRepository {
    Mono<ReferralProgram> save(ReferralProgram program);
    Mono<ReferralProgram> findById(TenantId tenantId, UUID programId);
    Mono<ReferralProgram> findActiveByTenantId(TenantId tenantId);
}
