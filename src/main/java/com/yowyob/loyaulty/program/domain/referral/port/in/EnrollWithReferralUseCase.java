package com.yowyob.loyaulty.program.domain.referral.port.in;

import com.yowyob.loyaulty.program.domain.referral.model.ReferralEvent;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

public interface EnrollWithReferralUseCase {
    Mono<ReferralEvent> enroll(TenantId tenantId, String refereeId, String referralCode);
}
