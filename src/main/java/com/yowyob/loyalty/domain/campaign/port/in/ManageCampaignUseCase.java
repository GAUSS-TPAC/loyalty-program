package com.yowyob.loyalty.domain.campaign.port.in;

import com.yowyob.loyalty.domain.campaign.model.Campaign;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ManageCampaignUseCase {
    Mono<Campaign> activate(TenantId tenantId, UUID campaignId);
    Mono<Campaign> pause(TenantId tenantId, UUID campaignId);
    Mono<Campaign> cancel(TenantId tenantId, UUID campaignId);
}
