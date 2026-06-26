package com.yowyob.loyalty.domain.referral.port.in;

import com.yowyob.loyalty.domain.referral.model.ReferralEvent;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import reactor.core.publisher.Mono;

public interface RegisterReferralUseCase {
    Mono<ReferralEvent> register(TenantId tenantId, String referralCode, UserId refereeId);
}
