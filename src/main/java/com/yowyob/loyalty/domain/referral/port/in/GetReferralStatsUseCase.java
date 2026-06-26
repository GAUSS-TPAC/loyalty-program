package com.yowyob.loyalty.domain.referral.port.in;

import com.yowyob.loyalty.domain.referral.model.ReferralEvent;
import com.yowyob.loyalty.domain.referral.model.ReferralLink;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GetReferralStatsUseCase {
    Mono<ReferralLink> getMyLink(TenantId tenantId, UserId referrerId);
    Flux<ReferralEvent> getMyReferrals(TenantId tenantId, UserId referrerId);
}
