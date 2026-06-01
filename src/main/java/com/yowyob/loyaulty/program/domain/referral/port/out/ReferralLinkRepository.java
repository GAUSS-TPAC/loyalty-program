package com.yowyob.loyaulty.program.domain.referral.port.out;

import com.yowyob.loyaulty.program.domain.referral.model.ReferralLink;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

public interface ReferralLinkRepository {
    Mono<ReferralLink> findByReferrerId(TenantId tenantId, String referrerId);
    Mono<ReferralLink> findByCode(TenantId tenantId, String code);
    Mono<ReferralLink> save(ReferralLink link);
}
