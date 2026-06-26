package com.yowyob.loyalty.application.promo.handler;

import com.yowyob.loyalty.domain.promo.model.PromoCampaign;
import com.yowyob.loyalty.domain.promo.port.in.GetPromoCampaignUseCase;
import com.yowyob.loyalty.domain.promo.port.in.ManagePromoCampaignUseCase;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class GetPromoCampaignHandler {

    private final GetPromoCampaignUseCase getUseCase;
    private final ManagePromoCampaignUseCase manageUseCase;

    public GetPromoCampaignHandler(GetPromoCampaignUseCase getUseCase, ManagePromoCampaignUseCase manageUseCase) {
        this.getUseCase = getUseCase;
        this.manageUseCase = manageUseCase;
    }

    public Mono<PromoCampaign> getById(TenantId tenantId, UUID campaignId) {
        return getUseCase.getById(tenantId, campaignId);
    }

    public Mono<PromoCampaign> getByCode(TenantId tenantId, String code) {
        return getUseCase.getByCode(tenantId, code);
    }

    public Flux<PromoCampaign> listAll(TenantId tenantId) {
        return getUseCase.listAll(tenantId);
    }

    public Flux<PromoCampaign> listActive(TenantId tenantId) {
        return getUseCase.listActive(tenantId);
    }

    public Mono<PromoCampaign> activate(TenantId tenantId, UUID campaignId) {
        return manageUseCase.activate(tenantId, campaignId);
    }

    public Mono<PromoCampaign> deactivate(TenantId tenantId, UUID campaignId) {
        return manageUseCase.deactivate(tenantId, campaignId);
    }

    public Mono<Void> delete(TenantId tenantId, UUID campaignId) {
        return manageUseCase.delete(tenantId, campaignId);
    }
}
