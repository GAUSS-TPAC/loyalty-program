package com.yowyob.loyalty.api.subscription;

import com.yowyob.loyalty.api.subscription.dto.request.ChangePlanRequest;
import com.yowyob.loyalty.api.subscription.dto.request.StartTrialRequest;
import com.yowyob.loyalty.api.subscription.dto.request.SubscribeRequest;
import com.yowyob.loyalty.api.subscription.dto.response.InvoiceRecordResponse;
import com.yowyob.loyalty.api.subscription.dto.response.TenantSubscriptionResponse;
import com.yowyob.loyalty.domain.subscription.model.BillingCycle;
import com.yowyob.loyalty.domain.subscription.port.in.GetSubscriptionUseCase;
import com.yowyob.loyalty.domain.subscription.port.in.SubscribeUseCase;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscriptions", description = "Gestion des abonnements tenant")
public class TenantSubscriptionController {

    private final SubscribeUseCase subscribeUseCase;
    private final GetSubscriptionUseCase getSubscriptionUseCase;

    public TenantSubscriptionController(SubscribeUseCase subscribeUseCase,
                                         GetSubscriptionUseCase getSubscriptionUseCase) {
        this.subscribeUseCase = subscribeUseCase;
        this.getSubscriptionUseCase = getSubscriptionUseCase;
    }

    @GetMapping("/me")
    public Mono<TenantSubscriptionResponse> getMySubscription() {
        return TenantContextHolder.getTenantId()
                .flatMap(getSubscriptionUseCase::getSubscription)
                .map(TenantSubscriptionResponse::from);
    }

    @GetMapping("/me/invoices")
    public Flux<InvoiceRecordResponse> getMyInvoices() {
        return TenantContextHolder.getTenantId()
                .flatMapMany(getSubscriptionUseCase::getInvoices)
                .map(InvoiceRecordResponse::from);
    }

    @PostMapping("/trial")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TenantSubscriptionResponse> startTrial(@Valid @RequestBody StartTrialRequest request) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> subscribeUseCase.startTrial(
                        tenantId, request.planId(),
                        request.trialDays() > 0 ? request.trialDays() : 14))
                .map(TenantSubscriptionResponse::from);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TenantSubscriptionResponse> subscribe(@Valid @RequestBody SubscribeRequest request) {
        BillingCycle cycle = request.billingCycle() != null
                ? BillingCycle.valueOf(request.billingCycle().toUpperCase())
                : BillingCycle.MONTHLY;
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> subscribeUseCase.subscribe(tenantId, request.planId(), cycle))
                .map(TenantSubscriptionResponse::from);
    }

    @PatchMapping("/me/plan")
    public Mono<TenantSubscriptionResponse> changePlan(@Valid @RequestBody ChangePlanRequest request) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> subscribeUseCase.changePlan(tenantId, request.newPlanId()))
                .map(TenantSubscriptionResponse::from);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> cancel() {
        return TenantContextHolder.getTenantId()
                .flatMap(subscribeUseCase::cancelSubscription)
                .then();
    }
}
