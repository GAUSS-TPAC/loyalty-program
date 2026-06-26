package com.yowyob.loyalty.domain.promo.port.in;

import com.yowyob.loyalty.domain.promo.model.PromoValidationResult;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface ValidatePromoCodeUseCase {
    Mono<PromoValidationResult> validate(TenantId tenantId, String code, UserId memberId, BigDecimal orderAmount);
}
