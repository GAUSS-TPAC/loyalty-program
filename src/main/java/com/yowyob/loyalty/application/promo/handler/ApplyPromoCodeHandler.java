package com.yowyob.loyalty.application.promo.handler;

import com.yowyob.loyalty.domain.promo.model.PromoUsage;
import com.yowyob.loyalty.domain.promo.port.in.ApplyPromoCodeUseCase;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
public class ApplyPromoCodeHandler {

    private final ApplyPromoCodeUseCase useCase;

    public ApplyPromoCodeHandler(ApplyPromoCodeUseCase useCase) {
        this.useCase = useCase;
    }

    public Mono<PromoUsage> handle(TenantId tenantId, String code, UserId memberId,
                                    String orderId, BigDecimal orderAmount) {
        return useCase.apply(tenantId, code, memberId, orderId, orderAmount);
    }
}
