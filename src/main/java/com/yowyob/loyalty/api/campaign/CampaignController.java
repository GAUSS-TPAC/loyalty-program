package com.yowyob.loyalty.api.campaign;

import com.yowyob.loyalty.api.campaign.dto.request.CreateCampaignRequest;
import com.yowyob.loyalty.api.campaign.dto.response.CampaignResponse;
import com.yowyob.loyalty.domain.campaign.port.in.CreateCampaignUseCase;
import com.yowyob.loyalty.domain.campaign.port.in.GetCampaignUseCase;
import com.yowyob.loyalty.domain.campaign.port.in.ManageCampaignUseCase;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/campaigns")
@Tag(name = "Campaigns", description = "Campagnes temporisées et boosts de points")
public class CampaignController {

    private final CreateCampaignUseCase createUseCase;
    private final GetCampaignUseCase getUseCase;
    private final ManageCampaignUseCase manageUseCase;

    public CampaignController(CreateCampaignUseCase createUseCase,
                               GetCampaignUseCase getUseCase,
                               ManageCampaignUseCase manageUseCase) {
        this.createUseCase = createUseCase;
        this.getUseCase = getUseCase;
        this.manageUseCase = manageUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CampaignResponse> create(@Valid @RequestBody CreateCampaignRequest request) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> createUseCase.createCampaign(
                        tenantId, request.name(), request.description(),
                        request.campaignType(), request.targetEventType(),
                        request.bonusMultiplier(), request.bonusPoints(),
                        request.startDate(), request.endDate()))
                .map(CampaignResponse::from);
    }

    @GetMapping
    public Flux<CampaignResponse> listAll() {
        return TenantContextHolder.getTenantId()
                .flatMapMany(getUseCase::listAll)
                .map(CampaignResponse::from);
    }

    @GetMapping("/active")
    public Flux<CampaignResponse> listActive() {
        return TenantContextHolder.getTenantId()
                .flatMapMany(getUseCase::listActive)
                .map(CampaignResponse::from);
    }

    @GetMapping("/{campaignId}")
    public Mono<CampaignResponse> getById(@PathVariable UUID campaignId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> getUseCase.getById(tenantId, campaignId))
                .map(CampaignResponse::from);
    }

    @PatchMapping("/{campaignId}/activate")
    public Mono<CampaignResponse> activate(@PathVariable UUID campaignId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> manageUseCase.activate(tenantId, campaignId))
                .map(CampaignResponse::from);
    }

    @PatchMapping("/{campaignId}/pause")
    public Mono<CampaignResponse> pause(@PathVariable UUID campaignId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> manageUseCase.pause(tenantId, campaignId))
                .map(CampaignResponse::from);
    }

    @PatchMapping("/{campaignId}/cancel")
    public Mono<CampaignResponse> cancel(@PathVariable UUID campaignId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> manageUseCase.cancel(tenantId, campaignId))
                .map(CampaignResponse::from);
    }
}
