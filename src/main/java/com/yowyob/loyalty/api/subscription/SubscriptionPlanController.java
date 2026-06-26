package com.yowyob.loyalty.api.subscription;

import com.yowyob.loyalty.api.subscription.dto.request.CreatePlanRequest;
import com.yowyob.loyalty.api.subscription.dto.response.SubscriptionPlanResponse;
import com.yowyob.loyalty.domain.subscription.model.PlanFeatures;
import com.yowyob.loyalty.domain.subscription.port.in.GetPlanUseCase;
import com.yowyob.loyalty.domain.subscription.port.in.ManagePlanUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscription-plans")
@Tag(name = "Subscription Plans", description = "Gestion des plans d'abonnement SaaS")
public class SubscriptionPlanController {

    private final GetPlanUseCase getPlanUseCase;
    private final ManagePlanUseCase managePlanUseCase;

    public SubscriptionPlanController(GetPlanUseCase getPlanUseCase, ManagePlanUseCase managePlanUseCase) {
        this.getPlanUseCase = getPlanUseCase;
        this.managePlanUseCase = managePlanUseCase;
    }

    @GetMapping
    public Flux<SubscriptionPlanResponse> listActive() {
        return getPlanUseCase.listActivePlans().map(SubscriptionPlanResponse::from);
    }

    @GetMapping("/{planId}")
    public Mono<SubscriptionPlanResponse> getById(@PathVariable UUID planId) {
        return getPlanUseCase.getPlanById(planId).map(SubscriptionPlanResponse::from);
    }

    @GetMapping("/code/{code}")
    public Mono<SubscriptionPlanResponse> getByCode(@PathVariable String code) {
        return getPlanUseCase.getPlanByCode(code).map(SubscriptionPlanResponse::from);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SubscriptionPlanResponse> create(@Valid @RequestBody CreatePlanRequest request) {
        PlanFeatures features = new PlanFeatures(
                request.maxRules(), request.maxMembers(), request.maxEventsPerMonth(),
                request.referralEnabled(), request.campaignsEnabled(),
                request.promoCodesEnabled(), request.analyticsEnabled()
        );
        return managePlanUseCase.createPlan(
                request.code(), request.name(), request.description(),
                request.priceMonthly(), request.priceYearly(),
                request.currency() != null ? request.currency() : "XAF",
                features
        ).map(SubscriptionPlanResponse::from);
    }

    @PatchMapping("/{planId}/activate")
    public Mono<SubscriptionPlanResponse> activate(@PathVariable UUID planId) {
        return managePlanUseCase.activatePlan(planId).map(SubscriptionPlanResponse::from);
    }

    @PatchMapping("/{planId}/deactivate")
    public Mono<SubscriptionPlanResponse> deactivate(@PathVariable UUID planId) {
        return managePlanUseCase.deactivatePlan(planId).map(SubscriptionPlanResponse::from);
    }
}
