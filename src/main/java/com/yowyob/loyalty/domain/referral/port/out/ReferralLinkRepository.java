package com.yowyob.loyalty.domain.referral.port.out;

import com.yowyob.loyalty.domain.referral.model.ReferralLink;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import reactor.core.publisher.Mono;

public interface ReferralLinkRepository {
    Mono<ReferralLink> save(ReferralLink link);
    Mono<ReferralLink> findByCode(TenantId tenantId, String code);
    Mono<ReferralLink> findByReferrerId(TenantId tenantId, UserId referrerId);
}
