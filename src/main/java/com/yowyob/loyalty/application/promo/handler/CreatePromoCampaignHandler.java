package com.yowyob.loyalty.application.promo.handler;

import com.yowyob.loyalty.domain.promo.model.PromoCampaign;
import com.yowyob.loyalty.domain.promo.model.PromoDiscountType;
import com.yowyob.loyalty.domain.promo.port.in.CreatePromoCampaignUseCase;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class CreatePromoCampaignHandler {

    private final CreatePromoCampaignUseCase useCase;

    public CreatePromoCampaignHandler(CreatePromoCampaignUseCase useCase) {
        this.useCase = useCase;
    }

    public Mono<PromoCampaign> handle(TenantId tenantId, String code, String name,
                                       PromoDiscountType discountType, BigDecimal discountValue,
                                       BigDecimal minOrderAmount, int maxUses, int perMemberLimit,
                                       Instant startDate, Instant endDate) {
        return useCase.createCampaign(tenantId, code, name, discountType, discountValue,
                minOrderAmount, maxUses, perMemberLimit, startDate, endDate);
    }
}
