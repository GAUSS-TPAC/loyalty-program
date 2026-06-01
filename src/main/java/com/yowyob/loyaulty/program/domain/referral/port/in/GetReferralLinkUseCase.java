package com.yowyob.loyaulty.program.domain.referral.port.in;

import com.yowyob.loyaulty.program.domain.referral.model.ReferralLink;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

public interface GetReferralLinkUseCase {
    Mono<ReferralLink> getOrCreate(TenantId tenantId, String memberId);
}
