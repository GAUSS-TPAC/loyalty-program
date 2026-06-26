package com.yowyob.loyalty.application.promo.handler;

import com.yowyob.loyalty.domain.promo.model.PromoValidationResult;
import com.yowyob.loyalty.domain.promo.port.in.ValidatePromoCodeUseCase;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
public class ValidatePromoCodeHandler {

    private final ValidatePromoCodeUseCase useCase;

    public ValidatePromoCodeHandler(ValidatePromoCodeUseCase useCase) {
        this.useCase = useCase;
    }

    public Mono<PromoValidationResult> handle(TenantId tenantId, String code, UserId memberId, BigDecimal orderAmount) {
        return useCase.validate(tenantId, code, memberId, orderAmount);
    }
}
