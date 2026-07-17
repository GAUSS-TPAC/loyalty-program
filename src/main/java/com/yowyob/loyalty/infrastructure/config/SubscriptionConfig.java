package com.yowyob.loyalty.infrastructure.config;

import com.yowyob.loyalty.application.subscription.scheduler.SubscriptionRenewalScheduler;
import com.yowyob.loyalty.domain.loyalty.port.out.PointsAccountRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.subscription.model.BillingCycle;
import com.yowyob.loyalty.domain.subscription.model.InvoiceRecord;
import com.yowyob.loyalty.domain.subscription.model.PlanFeatures;
import com.yowyob.loyalty.domain.subscription.model.PlatformTenantSummary;
import com.yowyob.loyalty.domain.subscription.model.SubscriptionPlan;
import com.yowyob.loyalty.domain.subscription.model.TenantSubscription;
import com.yowyob.loyalty.domain.subscription.port.in.*;
import com.yowyob.loyalty.domain.subscription.port.out.*;
import com.yowyob.loyalty.domain.subscription.service.SubscriptionDomainService;
import com.yowyob.loyalty.domain.tenant.port.out.TenantDirectoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Configuration
@EnableScheduling
public class SubscriptionConfig {

    @Bean
    public SubscriptionDomainService subscriptionDomainService(
            SubscriptionPlanRepository planRepository,
            TenantSubscriptionRepository subscriptionRepository,
            InvoiceRepository invoiceRepository,
            TenantDirectoryPort tenantDirectoryPort,
            PointsAccountRepository pointsAccountRepository) {
        return new SubscriptionDomainService(planRepository, subscriptionRepository, invoiceRepository,
                tenantDirectoryPort, pointsAccountRepository);
    }

    // SubscriptionDomainService implements all 5 use-case interfaces below directly, so
    // re-exposing it verbatim under each interface type makes every explicit bean also
    // match every *other* interface (and the raw subscriptionDomainService bean too),
    // causing NoUniqueBeanDefinitionException. Single-interface adapters here, plus
    // @Primary, keep exactly one unambiguous candidate per interface type.

    @Bean
    @Primary
    public GetPlanUseCase getPlanUseCase(SubscriptionDomainService service) {
        return new GetPlanUseCase() {
            @Override
            public Flux<SubscriptionPlan> listActivePlans() {
                return service.listActivePlans();
            }

            @Override
            public Mono<SubscriptionPlan> getPlanById(UUID planId) {
                return service.getPlanById(planId);
            }

            @Override
            public Mono<SubscriptionPlan> getPlanByCode(String code) {
                return service.getPlanByCode(code);
            }
        };
    }

    @Bean
    @Primary
    public ManagePlanUseCase managePlanUseCase(SubscriptionDomainService service) {
        return new ManagePlanUseCase() {
            @Override
            public Mono<SubscriptionPlan> createPlan(String code, String name, String description,
                                                      BigDecimal priceMonthly, BigDecimal priceYearly,
                                                      String currency, PlanFeatures features) {
                return service.createPlan(code, name, description, priceMonthly, priceYearly, currency, features);
            }

            @Override
            public Mono<SubscriptionPlan> activatePlan(UUID planId) {
                return service.activatePlan(planId);
            }

            @Override
            public Mono<SubscriptionPlan> deactivatePlan(UUID planId) {
                return service.deactivatePlan(planId);
            }
        };
    }

    @Bean
    @Primary
    public SubscribeUseCase subscribeUseCase(SubscriptionDomainService service) {
        return new SubscribeUseCase() {
            @Override
            public Mono<TenantSubscription> startTrial(TenantId tenantId, UUID planId, int trialDays) {
                return service.startTrial(tenantId, planId, trialDays);
            }

            @Override
            public Mono<TenantSubscription> subscribe(TenantId tenantId, UUID planId, BillingCycle cycle) {
                return service.subscribe(tenantId, planId, cycle);
            }

            @Override
            public Mono<TenantSubscription> changePlan(TenantId tenantId, UUID newPlanId) {
                return service.changePlan(tenantId, newPlanId);
            }

            @Override
            public Mono<TenantSubscription> cancelSubscription(TenantId tenantId) {
                return service.cancelSubscription(tenantId);
            }
        };
    }

    @Bean
    @Primary
    public GetSubscriptionUseCase getSubscriptionUseCase(SubscriptionDomainService service) {
        return new GetSubscriptionUseCase() {
            @Override
            public Mono<TenantSubscription> getSubscription(TenantId tenantId) {
                return service.getSubscription(tenantId);
            }

            @Override
            public Mono<TenantSubscription> getSubscriptionById(UUID id) {
                return service.getSubscriptionById(id);
            }

            @Override
            public Flux<InvoiceRecord> getInvoices(TenantId tenantId) {
                return service.getInvoices(tenantId);
            }
        };
    }

    @Bean
    @Primary
    public ProcessSubscriptionRenewalUseCase processSubscriptionRenewalUseCase(SubscriptionDomainService service) {
        return new ProcessSubscriptionRenewalUseCase() {
            @Override
            public Mono<Integer> processExpiredTrials() {
                return service.processExpiredTrials();
            }

            @Override
            public Mono<Integer> processExpiredSubscriptions() {
                return service.processExpiredSubscriptions();
            }

            @Override
            public Mono<Integer> processOverdueInvoices() {
                return service.processOverdueInvoices();
            }
        };
    }

    @Bean
    @Primary
    public ListPlatformTenantsUseCase listPlatformTenantsUseCase(SubscriptionDomainService service) {
        return new ListPlatformTenantsUseCase() {
            @Override
            public Flux<PlatformTenantSummary> listSubscribedTenants() {
                return service.listSubscribedTenants();
            }
        };
    }

    @Bean
    public SubscriptionRenewalScheduler subscriptionRenewalScheduler(ProcessSubscriptionRenewalUseCase renewalUseCase) {
        return new SubscriptionRenewalScheduler(renewalUseCase);
    }
}
