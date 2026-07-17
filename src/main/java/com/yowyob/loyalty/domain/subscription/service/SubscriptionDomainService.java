package com.yowyob.loyalty.domain.subscription.service;

import com.yowyob.loyalty.domain.loyalty.port.out.PointsAccountRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.subscription.exception.*;
import com.yowyob.loyalty.domain.subscription.model.*;
import com.yowyob.loyalty.domain.subscription.port.in.*;
import com.yowyob.loyalty.domain.subscription.port.out.*;
import com.yowyob.loyalty.domain.tenant.port.out.TenantDirectoryPort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class SubscriptionDomainService implements
        GetPlanUseCase, ManagePlanUseCase, SubscribeUseCase,
        GetSubscriptionUseCase, ProcessSubscriptionRenewalUseCase, ListPlatformTenantsUseCase {

    private final SubscriptionPlanRepository planRepository;
    private final TenantSubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final TenantDirectoryPort tenantDirectoryPort;
    private final PointsAccountRepository pointsAccountRepository;

    public SubscriptionDomainService(SubscriptionPlanRepository planRepository,
                                      TenantSubscriptionRepository subscriptionRepository,
                                      InvoiceRepository invoiceRepository,
                                      TenantDirectoryPort tenantDirectoryPort,
                                      PointsAccountRepository pointsAccountRepository) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
        this.tenantDirectoryPort = tenantDirectoryPort;
        this.pointsAccountRepository = pointsAccountRepository;
    }

    // ── Plans ─────────────────────────────────────────────────────────────────

    @Override
    public Flux<SubscriptionPlan> listActivePlans() {
        return planRepository.findAllActive();
    }

    @Override
    public Mono<SubscriptionPlan> getPlanById(UUID planId) {
        return planRepository.findById(planId)
                .switchIfEmpty(Mono.error(new PlanNotFoundException(planId)));
    }

    @Override
    public Mono<SubscriptionPlan> getPlanByCode(String code) {
        return planRepository.findByCode(code.toUpperCase())
                .switchIfEmpty(Mono.error(new PlanNotFoundException(code)));
    }

    @Override
    public Mono<SubscriptionPlan> createPlan(String code, String name, String description,
                                              BigDecimal priceMonthly, BigDecimal priceYearly,
                                              String currency, PlanFeatures features) {
        SubscriptionPlan plan = SubscriptionPlan.create(code, name, description,
                priceMonthly, priceYearly, currency, features);
        return planRepository.save(plan);
    }

    @Override
    public Mono<SubscriptionPlan> activatePlan(UUID planId) {
        return getPlanById(planId).flatMap(plan -> {
            plan.activate();
            return planRepository.save(plan);
        });
    }

    @Override
    public Mono<SubscriptionPlan> deactivatePlan(UUID planId) {
        return getPlanById(planId).flatMap(plan -> {
            plan.deactivate();
            return planRepository.save(plan);
        });
    }

    // ── Subscriptions ─────────────────────────────────────────────────────────

    @Override
    public Mono<TenantSubscription> startTrial(TenantId tenantId, UUID planId, int trialDays) {
        return subscriptionRepository.findByTenantId(tenantId)
                .flatMap(existing -> Mono.<TenantSubscription>error(
                        new AlreadySubscribedException(tenantId.value().toString())))
                .switchIfEmpty(
                        getPlanById(planId).flatMap(plan -> {
                            TenantSubscription sub = TenantSubscription.createTrial(tenantId, planId, trialDays);
                            return subscriptionRepository.save(sub);
                        })
                );
    }

    @Override
    public Mono<TenantSubscription> subscribe(TenantId tenantId, UUID planId, BillingCycle cycle) {
        return subscriptionRepository.findByTenantId(tenantId)
                .flatMap(existing -> {
                    if (existing.status() == SubscriptionStatus.TRIAL || existing.status() == SubscriptionStatus.PAST_DUE) {
                        existing.changePlan(planId);
                        existing.activate();
                        existing.renew();
                        // La ligne subscription doit exister en base avant l'insertion de la facture
                        // (invoice_records.subscription_id référence subscriptions.id par FK).
                        return subscriptionRepository.save(existing)
                                .flatMap(saved -> generateInvoice(saved, planId, cycle).thenReturn(saved));
                    }
                    if (existing.status().isTerminal()) {
                        // allow resubscribe after cancellation/expiry
                        TenantSubscription sub = TenantSubscription.createActive(tenantId, planId, cycle);
                        return subscriptionRepository.save(sub)
                                .flatMap(saved -> generateInvoice(saved, planId, cycle).thenReturn(saved));
                    }
                    return Mono.<TenantSubscription>error(
                            new AlreadySubscribedException(tenantId.value().toString()));
                })
                .switchIfEmpty(
                        getPlanById(planId).flatMap(plan -> {
                            TenantSubscription sub = TenantSubscription.createActive(tenantId, planId, cycle);
                            return subscriptionRepository.save(sub)
                                    .flatMap(saved -> generateInvoice(saved, planId, cycle).thenReturn(saved));
                        })
                );
    }

    @Override
    public Mono<TenantSubscription> changePlan(TenantId tenantId, UUID newPlanId) {
        return getSubscription(tenantId).flatMap(sub -> {
            if (sub.status().isTerminal()) {
                return Mono.error(new SubscriptionAlreadyTerminalException(sub.id()));
            }
            return getPlanById(newPlanId).flatMap(plan -> {
                sub.changePlan(newPlanId);
                return subscriptionRepository.save(sub);
            });
        });
    }

    @Override
    public Mono<TenantSubscription> cancelSubscription(TenantId tenantId) {
        return getSubscription(tenantId).flatMap(sub -> {
            if (sub.status().isTerminal()) {
                return Mono.error(new SubscriptionAlreadyTerminalException(sub.id()));
            }
            sub.cancel();
            return subscriptionRepository.save(sub);
        });
    }

    @Override
    public Mono<TenantSubscription> getSubscription(TenantId tenantId) {
        return subscriptionRepository.findByTenantId(tenantId)
                .switchIfEmpty(Mono.error(new SubscriptionNotFoundException(tenantId.value().toString())));
    }

    @Override
    public Mono<TenantSubscription> getSubscriptionById(UUID id) {
        return subscriptionRepository.findById(id)
                .switchIfEmpty(Mono.error(new SubscriptionNotFoundException(id)));
    }

    @Override
    public Flux<InvoiceRecord> getInvoices(TenantId tenantId) {
        return invoiceRepository.findByTenantId(tenantId);
    }

    // ── Schedulers ────────────────────────────────────────────────────────────

    @Override
    public Mono<Integer> processExpiredTrials() {
        AtomicInteger count = new AtomicInteger(0);
        return subscriptionRepository.findExpiredTrials(Instant.now())
                .flatMap(sub -> {
                    sub.markExpired();
                    return subscriptionRepository.save(sub);
                })
                .doOnNext(s -> count.incrementAndGet())
                .then(Mono.fromSupplier(count::get));
    }

    @Override
    public Mono<Integer> processExpiredSubscriptions() {
        AtomicInteger count = new AtomicInteger(0);
        return subscriptionRepository.findExpiredActive(Instant.now())
                .flatMap(sub -> {
                    sub.markPastDue();
                    return subscriptionRepository.save(sub);
                })
                .doOnNext(s -> count.incrementAndGet())
                .then(Mono.fromSupplier(count::get));
    }

    @Override
    public Mono<Integer> processOverdueInvoices() {
        AtomicInteger count = new AtomicInteger(0);
        return invoiceRepository.findOverduePending(Instant.now())
                .flatMap(invoice -> {
                    invoice.markFailed();
                    return invoiceRepository.save(invoice);
                })
                .doOnNext(i -> count.incrementAndGet())
                .then(Mono.fromSupplier(count::get));
    }

    // ── Console plateforme ───────────────────────────────────────────────────────

    @Override
    public Flux<PlatformTenantSummary> listSubscribedTenants() {
        return subscriptionRepository.findAll()
                .flatMap(sub -> Mono.zip(
                        planRepository.findById(sub.planId())
                                .map(plan -> new String[]{plan.code(), plan.name(), plan.currency()})
                                .defaultIfEmpty(new String[]{"—", "Plan supprimé", "—"}),
                        invoiceRepository.findByTenantId(sub.tenantId())
                                .filter(inv -> inv.status() == InvoiceStatus.PAID)
                                .map(InvoiceRecord::amount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                        tenantDirectoryPort.resolveTenant(sub.tenantId())
                                .map(com.yowyob.loyalty.domain.tenant.model.Tenant::getName)
                                .onErrorReturn("Tenant " + sub.tenantId().value())
                                .defaultIfEmpty("Tenant " + sub.tenantId().value()),
                        pointsAccountRepository.sumLifetimeEarnedByTenant(sub.tenantId())
                                .defaultIfEmpty(0L)
                ).map(t -> new PlatformTenantSummary(
                        sub.tenantId(), t.getT3(), sub, t.getT1()[0], t.getT1()[1], t.getT2(), t.getT1()[2], t.getT4()
                )));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Mono<InvoiceRecord> generateInvoice(TenantSubscription sub, UUID planId, BillingCycle cycle) {
        return planRepository.findById(planId).flatMap(plan -> {
            BigDecimal amount = plan.priceFor(cycle);
            InvoiceRecord invoice = InvoiceRecord.generate(
                    sub.tenantId(), sub.id(), planId, amount, plan.currency(),
                    sub.currentPeriodStart(), sub.currentPeriodEnd());
            return invoiceRepository.save(invoice);
        });
    }
}
