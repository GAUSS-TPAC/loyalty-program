package com.yowyob.loyalty.domain.promo.port.in;

import com.yowyob.loyalty.domain.promo.model.PromoUsage;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface ApplyPromoCodeUseCase {
    Mono<PromoUsage> apply(TenantId tenantId, String code, UserId memberId, String orderId, BigDecimal orderAmount);
}
