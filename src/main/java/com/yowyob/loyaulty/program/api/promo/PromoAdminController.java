package com.yowyob.loyaulty.program.api.promo;

import com.yowyob.loyaulty.program.api.promo.dto.request.CreateCampaignRequest;
import com.yowyob.loyaulty.program.domain.promo.model.PromoCampaign;
import com.yowyob.loyaulty.program.domain.promo.port.in.ValidatePromoUseCase;
import com.yowyob.loyaulty.program.shared.multitenancy.TenantContextHolder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/promos")
public class PromoAdminController {

    private final ValidatePromoUseCase promoUseCase;

    public PromoAdminController(ValidatePromoUseCase promoUseCase) {
        this.promoUseCase = promoUseCase;
    }

    @GetMapping
    public Flux<Map<String, Object>> listCampaigns() {
        return TenantContextHolder.getTenantId()
                .flatMapMany(promoUseCase::listCampaigns)
                .map(this::toMap);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Map<String, Object>> createCampaign(@Valid @RequestBody CreateCampaignRequest req) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> promoUseCase.createCampaign(
                        tenantId, req.name(), req.code(), req.discountType(),
                        req.discountValue(), req.maxUsesTotal(), req.maxUsesPerMember(),
                        req.minOrderAmount(), req.validFrom(), req.validUntil()))
                .map(this::toMap);
    }

    @PostMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> activate(@PathVariable UUID id) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> promoUseCase.activateCampaign(tenantId, id))
                .then();
    }

    private Map<String, Object> toMap(PromoCampaign c) {
        return Map.of(
                "id", c.getId(),
                "name", c.getName(),
                "code", c.getCode() != null ? c.getCode() : "",
                "discountType", c.getDiscountType().name(),
                "discountValue", c.getDiscountValue(),
                "status", c.getStatus().name()
        );
    }
}
