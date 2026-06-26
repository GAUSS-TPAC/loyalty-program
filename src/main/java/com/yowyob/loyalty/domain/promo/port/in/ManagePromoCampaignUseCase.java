package com.yowyob.loyalty.domain.promo.port.in;

import com.yowyob.loyalty.domain.promo.model.PromoCampaign;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ManagePromoCampaignUseCase {
    Mono<PromoCampaign> activate(TenantId tenantId, UUID campaignId);
    Mono<PromoCampaign> deactivate(TenantId tenantId, UUID campaignId);
    Mono<Void> delete(TenantId tenantId, UUID campaignId);
}
