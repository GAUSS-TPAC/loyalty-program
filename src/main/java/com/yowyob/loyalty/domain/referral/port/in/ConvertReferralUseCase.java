package com.yowyob.loyalty.domain.referral.port.in;

import com.yowyob.loyalty.domain.referral.model.ReferralEvent;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface ConvertReferralUseCase {
    Mono<ReferralEvent> convert(TenantId tenantId, UserId refereeId, BigDecimal conversionAmount);
}
