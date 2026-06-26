package com.yowyob.loyalty.domain.promo.port.in;

import com.yowyob.loyalty.domain.promo.model.PromoCampaign;
import com.yowyob.loyalty.domain.promo.model.PromoDiscountType;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

public interface CreatePromoCampaignUseCase {
    Mono<PromoCampaign> createCampaign(TenantId tenantId, String code, String name,
                                        PromoDiscountType discountType, BigDecimal discountValue,
                                        BigDecimal minOrderAmount, int maxUses, int perMemberLimit,
                                        Instant startDate, Instant endDate);
}
