package com.yowyob.loyalty.domain.campaign.port.in;

import com.yowyob.loyalty.domain.campaign.model.Campaign;
import com.yowyob.loyalty.domain.campaign.model.CampaignType;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

public interface CreateCampaignUseCase {
    Mono<Campaign> createCampaign(TenantId tenantId, String name, String description,
                                   CampaignType type, String targetEventType,
                                   BigDecimal bonusMultiplier, long bonusPoints,
                                   Instant startDate, Instant endDate);
}
