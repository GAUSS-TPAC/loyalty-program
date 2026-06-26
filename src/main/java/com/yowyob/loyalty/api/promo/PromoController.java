package com.yowyob.loyalty.api.promo;

import com.yowyob.loyalty.api.promo.dto.request.ApplyPromoCodeRequest;
import com.yowyob.loyalty.api.promo.dto.request.CreatePromoCampaignRequest;
import com.yowyob.loyalty.api.promo.dto.request.ValidatePromoCodeRequest;
import com.yowyob.loyalty.api.promo.dto.response.PromoApplicationResponse;
import com.yowyob.loyalty.api.promo.dto.response.PromoCampaignResponse;
import com.yowyob.loyalty.api.promo.dto.response.PromoValidationResponse;
import com.yowyob.loyalty.application.promo.handler.ApplyPromoCodeHandler;
import com.yowyob.loyalty.application.promo.handler.CreatePromoCampaignHandler;
import com.yowyob.loyalty.application.promo.handler.GetPromoCampaignHandler;
import com.yowyob.loyalty.application.promo.handler.ValidatePromoCodeHandler;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/promo")
@Tag(name = "Promo", description = "Codes promotionnels")
public class PromoController {

    private final CreatePromoCampaignHandler createHandler;
    private final ValidatePromoCodeHandler validateHandler;
    private final ApplyPromoCodeHandler applyHandler;
    private final GetPromoCampaignHandler getHandler;

    public PromoController(CreatePromoCampaignHandler createHandler,
                           ValidatePromoCodeHandler validateHandler,
                           ApplyPromoCodeHandler applyHandler,
                           GetPromoCampaignHandler getHandler) {
        this.createHandler = createHandler;
        this.validateHandler = validateHandler;
        this.applyHandler = applyHandler;
        this.getHandler = getHandler;
    }

    // ── Admin endpoints ──────────────────────────────────────────────────────

    @PostMapping("/admin/campaigns")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PromoCampaignResponse> createCampaign(@Valid @RequestBody CreatePromoCampaignRequest request) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> createHandler.handle(
                        tenantId, request.code(), request.name(),
                        request.discountType(), request.discountValue(),
                        request.minOrderAmount(), request.maxUses(), request.perMemberLimit(),
                        request.startDate(), request.endDate()))
                .map(PromoCampaignResponse::from);
    }

    @GetMapping("/admin/campaigns")
    public Flux<PromoCampaignResponse> listAll() {
        return TenantContextHolder.getTenantId()
                .flatMapMany(getHandler::listAll)
                .map(PromoCampaignResponse::from);
    }

    @GetMapping("/admin/campaigns/{campaignId}")
    public Mono<PromoCampaignResponse> getById(@PathVariable UUID campaignId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> getHandler.getById(tenantId, campaignId))
                .map(PromoCampaignResponse::from);
    }

    @PatchMapping("/admin/campaigns/{campaignId}/activate")
    public Mono<PromoCampaignResponse> activate(@PathVariable UUID campaignId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> getHandler.activate(tenantId, campaignId))
                .map(PromoCampaignResponse::from);
    }

    @PatchMapping("/admin/campaigns/{campaignId}/deactivate")
    public Mono<PromoCampaignResponse> deactivate(@PathVariable UUID campaignId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> getHandler.deactivate(tenantId, campaignId))
                .map(PromoCampaignResponse::from);
    }

    @DeleteMapping("/admin/campaigns/{campaignId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable UUID campaignId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> getHandler.delete(tenantId, campaignId));
    }

    // ── Member endpoints ─────────────────────────────────────────────────────

    @GetMapping("/campaigns")
    public Flux<PromoCampaignResponse> listActiveCampaigns() {
        return TenantContextHolder.getTenantId()
                .flatMapMany(getHandler::listActive)
                .map(PromoCampaignResponse::from);
    }

    @GetMapping("/campaigns/{code}")
    public Mono<PromoCampaignResponse> getByCode(@PathVariable String code) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> getHandler.getByCode(tenantId, code))
                .map(PromoCampaignResponse::from);
    }

    @PostMapping("/validate")
    public Mono<PromoValidationResponse> validate(@Valid @RequestBody ValidatePromoCodeRequest request) {
        return Mono.zip(
                TenantContextHolder.getTenantId(),
                ReactiveSecurityContextHolder.getContext()
                        .map(ctx -> UserId.of(ctx.getAuthentication().getName()))
        ).flatMap(tuple -> validateHandler.handle(
                tuple.getT1(), request.code(), tuple.getT2(), request.orderAmount()))
        .map(PromoValidationResponse::from);
    }

    @PostMapping("/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PromoApplicationResponse> apply(@Valid @RequestBody ApplyPromoCodeRequest request) {
        return Mono.zip(
                TenantContextHolder.getTenantId(),
                ReactiveSecurityContextHolder.getContext()
                        .map(ctx -> UserId.of(ctx.getAuthentication().getName()))
        ).flatMap(tuple -> applyHandler.handle(
                tuple.getT1(), request.code(), tuple.getT2(),
                request.orderId(), request.orderAmount()))
        .map(PromoApplicationResponse::from);
    }
}
