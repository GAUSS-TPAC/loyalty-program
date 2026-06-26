package com.yowyob.loyalty.domain.referral.port.in;

import com.yowyob.loyalty.domain.referral.model.ReferralLink;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import reactor.core.publisher.Mono;

public interface CreateReferralLinkUseCase {
    Mono<ReferralLink> createLink(TenantId tenantId, UserId referrerId);
}
