package com.yowyob.loyalty.domain.subscription;

import com.yowyob.loyalty.domain.loyalty.model.points.PointsAccount;
import com.yowyob.loyalty.domain.loyalty.port.out.PointsAccountRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.subscription.exception.*;
import com.yowyob.loyalty.domain.subscription.model.*;
import com.yowyob.loyalty.domain.subscription.port.out.InvoiceRepository;
import com.yowyob.loyalty.domain.subscription.port.out.SubscriptionPlanRepository;
import com.yowyob.loyalty.domain.subscription.port.out.TenantSubscriptionRepository;
import com.yowyob.loyalty.domain.subscription.service.SubscriptionDomainService;
import com.yowyob.loyalty.domain.tenant.port.out.TenantDirectoryPort;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SubscriptionDomainServiceTest {

    private final Map<UUID, SubscriptionPlan> plans = new ConcurrentHashMap<>();
    private final Map<UUID, TenantSubscription> subscriptionsById = new ConcurrentHashMap<>();
    private final Map<UUID, TenantSubscription> subscriptionsByTenant = new ConcurrentHashMap<>();
    private final java.util.List<InvoiceRecord> invoices = new java.util.concurrent.CopyOnWriteArrayList<>();

    private SubscriptionDomainService service;
    private final TenantId tenantId = new TenantId(UUID.randomUUID());
    private SubscriptionPlan plan;

    @BeforeEach
    void setup() {
        SubscriptionPlanRepository planRepository = new SubscriptionPlanRepository() {
            public Mono<SubscriptionPlan> save(SubscriptionPlan p) { plans.put(p.id(), p); return Mono.just(p); }
            public Mono<SubscriptionPlan> findById(UUID id) { return Mono.justOrEmpty(plans.get(id)); }
            public Mono<SubscriptionPlan> findByCode(String code) {
                return Flux.fromIterable(plans.values()).filter(p -> p.code().equals(code)).next();
            }
            public Flux<SubscriptionPlan> findAllActive() { return Flux.fromIterable(plans.values()).filter(SubscriptionPlan::active); }
            public Flux<SubscriptionPlan> findAll() { return Flux.fromIterable(plans.values()); }
        };
        TenantSubscriptionRepository subscriptionRepository = new TenantSubscriptionRepository() {
            public Mono<TenantSubscription> save(TenantSubscription s) {
                subscriptionsById.put(s.id(), s);
                subscriptionsByTenant.put(s.tenantId().value(), s);
                return Mono.just(s);
            }
            public Mono<TenantSubscription> findByTenantId(TenantId t) { return Mono.justOrEmpty(subscriptionsByTenant.get(t.value())); }
            public Mono<TenantSubscription> findById(UUID id) { return Mono.justOrEmpty(subscriptionsById.get(id)); }
            public Flux<TenantSubscription> findExpiredTrials(Instant now) {
                return Flux.fromIterable(subscriptionsById.values())
                        .filter(s -> s.status() == SubscriptionStatus.TRIAL && s.isTrialExpired(now));
            }
            public Flux<TenantSubscription> findExpiredActive(Instant now) {
                return Flux.fromIterable(subscriptionsById.values())
                        .filter(s -> s.status() == SubscriptionStatus.ACTIVE && s.isExpired(now));
            }
            public Flux<TenantSubscription> findAll() { return Flux.fromIterable(subscriptionsById.values()); }
        };
        InvoiceRepository invoiceRepository = new InvoiceRepository() {
            public Mono<InvoiceRecord> save(InvoiceRecord i) { invoices.add(i); return Mono.just(i); }
            public Flux<InvoiceRecord> findByTenantId(TenantId t) { return Flux.fromIterable(invoices); }
            public Flux<InvoiceRecord> findOverduePending(Instant now) {
                return Flux.fromIterable(invoices).filter(i -> i.isOverdue(now));
            }
            public Flux<InvoiceRecord> findAll() { return Flux.fromIterable(invoices); }
        };
        TenantDirectoryPort tenantDirectoryPort = t -> Mono.empty();
        PointsAccountRepository pointsAccountRepository = new PointsAccountRepository() {
            public PointsAccount save(PointsAccount a) { return a; }
            public Optional<PointsAccount> findById(UUID id) { return Optional.empty(); }
            public Optional<PointsAccount> findByMemberId(TenantId t, UserId m) { return Optional.empty(); }
            public Mono<Long> sumLifetimeEarnedByTenant(TenantId t) { return Mono.just(0L); }
        };

        service = new SubscriptionDomainService(planRepository, subscriptionRepository, invoiceRepository,
                tenantDirectoryPort, pointsAccountRepository);
        plan = SubscriptionPlan.create("PRO", "Pro", "desc", new BigDecimal("10"), new BigDecimal("100"), "XAF",
                new PlanFeatures(50, 1000, 100000, true, true, true, true));
        plans.put(plan.id(), plan);
    }

    @Test
    void startTrial_unknownPlan_throwsPlanNotFound() {
        StepVerifier.create(service.startTrial(tenantId, UUID.randomUUID(), 14))
                .expectError(PlanNotFoundException.class)
                .verify();
    }

    @Test
    void startTrial_anyExistingSubscriptionBlocksNewTrial_evenIfCancelled() {
        TenantSubscription cancelled = TenantSubscription.createActive(tenantId, plan.id(), BillingCycle.MONTHLY);
        cancelled.cancel();
        subscriptionsByTenant.put(tenantId.value(), cancelled);
        subscriptionsById.put(cancelled.id(), cancelled);

        // Documented quirk: startTrial blocks on ANY pre-existing subscription regardless of its
        // status (even a terminal CANCELLED one) -- unlike subscribe()'s more permissive logic.
        StepVerifier.create(service.startTrial(tenantId, plan.id(), 14))
                .expectError(AlreadySubscribedException.class)
                .verify();
    }

    @Test
    void subscribe_noExistingSubscription_createsActiveSubscriptionAndInvoice() {
        StepVerifier.create(service.subscribe(tenantId, plan.id(), BillingCycle.MONTHLY))
                .assertNext(sub -> assertEquals(SubscriptionStatus.ACTIVE, sub.status()))
                .verifyComplete();
        assertEquals(1, invoices.size());
    }

    @Test
    void subscribe_fromTrial_upgradesInPlaceKeepingSameId() {
        TenantSubscription trial = TenantSubscription.createTrial(tenantId, plan.id(), 14);
        subscriptionsByTenant.put(tenantId.value(), trial);
        subscriptionsById.put(trial.id(), trial);

        TenantSubscription upgraded = service.subscribe(tenantId, plan.id(), BillingCycle.YEARLY).block();

        assertEquals(trial.id(), upgraded.id(), "upgrading from TRIAL must reuse the existing subscription id");
        assertEquals(SubscriptionStatus.ACTIVE, upgraded.status());
    }

    @Test
    void subscribe_fromTerminalStatus_createsBrandNewSubscriptionWithNewId() {
        TenantSubscription cancelled = TenantSubscription.createActive(tenantId, plan.id(), BillingCycle.MONTHLY);
        cancelled.cancel();
        subscriptionsByTenant.put(tenantId.value(), cancelled);
        subscriptionsById.put(cancelled.id(), cancelled);

        TenantSubscription resubscribed = service.subscribe(tenantId, plan.id(), BillingCycle.MONTHLY).block();

        assertNotEquals(cancelled.id(), resubscribed.id(), "resubscribing after CANCELLED must create a brand-new subscription");
        assertEquals(SubscriptionStatus.ACTIVE, resubscribed.status());
    }

    @Test
    void subscribe_alreadyActive_throwsAlreadySubscribed() {
        TenantSubscription active = TenantSubscription.createActive(tenantId, plan.id(), BillingCycle.MONTHLY);
        subscriptionsByTenant.put(tenantId.value(), active);
        subscriptionsById.put(active.id(), active);

        StepVerifier.create(service.subscribe(tenantId, plan.id(), BillingCycle.MONTHLY))
                .expectError(AlreadySubscribedException.class)
                .verify();
    }

    @Test
    void changePlan_doesNotRenewOrReactivate_onlySwapsThePlanId() {
        TenantSubscription active = TenantSubscription.createActive(tenantId, plan.id(), BillingCycle.MONTHLY);
        active.markPastDue();
        subscriptionsByTenant.put(tenantId.value(), active);
        subscriptionsById.put(active.id(), active);

        SubscriptionPlan newPlan = SubscriptionPlan.create("ENT", "Enterprise", "desc",
                new BigDecimal("50"), new BigDecimal("500"), "XAF", new PlanFeatures(-1, 0, 0, true, true, true, true));
        plans.put(newPlan.id(), newPlan);

        TenantSubscription updated = service.changePlan(tenantId, newPlan.id()).block();

        assertEquals(newPlan.id(), updated.planId());
        assertEquals(SubscriptionStatus.PAST_DUE, updated.status(), "changePlan must not call activate()/renew() itself");
    }

    @Test
    void changePlan_onTerminalSubscription_throws() {
        TenantSubscription cancelled = TenantSubscription.createActive(tenantId, plan.id(), BillingCycle.MONTHLY);
        cancelled.cancel();
        subscriptionsByTenant.put(tenantId.value(), cancelled);
        subscriptionsById.put(cancelled.id(), cancelled);

        StepVerifier.create(service.changePlan(tenantId, UUID.randomUUID()))
                .expectError(SubscriptionAlreadyTerminalException.class)
                .verify();
    }

    @Test
    void cancelSubscription_onTerminalSubscription_throwsBeforeReachingDomainCancel() {
        TenantSubscription cancelled = TenantSubscription.createActive(tenantId, plan.id(), BillingCycle.MONTHLY);
        cancelled.cancel();
        subscriptionsByTenant.put(tenantId.value(), cancelled);
        subscriptionsById.put(cancelled.id(), cancelled);

        StepVerifier.create(service.cancelSubscription(tenantId))
                .expectError(SubscriptionAlreadyTerminalException.class)
                .verify();
    }

    @Test
    void getSubscription_none_throwsNotFoundWithTenantMessage() {
        StepVerifier.create(service.getSubscription(tenantId))
                .expectError(SubscriptionNotFoundException.class)
                .verify();
    }

    @Test
    void processExpiredTrials_marksExpiredNotPastDue() {
        TenantSubscription expiredTrial = TenantSubscription.createTrial(tenantId, plan.id(), -1); // already expired
        subscriptionsById.put(expiredTrial.id(), expiredTrial);

        Integer processed = service.processExpiredTrials().block();

        assertEquals(1, processed);
        assertEquals(SubscriptionStatus.EXPIRED, subscriptionsById.get(expiredTrial.id()).status());
    }

    @Test
    void processExpiredSubscriptions_marksPastDueNotExpired() {
        // Documented quirk: "expired active" subscriptions are marked PAST_DUE, not EXPIRED --
        // easy to misread from the method name alone.
        TenantSubscription active = TenantSubscription.createActive(tenantId, plan.id(), BillingCycle.MONTHLY);
        TenantSubscription expiredActive = TenantSubscription.reconstruct(active.id(), tenantId, plan.id(),
                SubscriptionStatus.ACTIVE, BillingCycle.MONTHLY, Instant.now().minusSeconds(1000),
                Instant.now().minusSeconds(1), null, null, Instant.now(), Instant.now(), 0L);
        subscriptionsById.put(expiredActive.id(), expiredActive);

        Integer processed = service.processExpiredSubscriptions().block();

        assertEquals(1, processed);
        assertEquals(SubscriptionStatus.PAST_DUE, subscriptionsById.get(expiredActive.id()).status());
    }

    @Test
    void processOverdueInvoices_marksFailed() {
        InvoiceRecord overdue = InvoiceRecord.reconstruct(UUID.randomUUID(), tenantId, UUID.randomUUID(), plan.id(),
                BigDecimal.TEN, "XAF", InvoiceStatus.PENDING, Instant.now(), Instant.now(),
                Instant.now().minusSeconds(1), null, Instant.now());
        invoices.add(overdue);

        Integer processed = service.processOverdueInvoices().block();

        assertEquals(1, processed);
        assertEquals(InvoiceStatus.FAILED, overdue.status());
    }
}
