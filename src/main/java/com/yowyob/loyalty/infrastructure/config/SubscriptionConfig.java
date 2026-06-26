package com.yowyob.loyalty.infrastructure.config;

import com.yowyob.loyalty.application.subscription.scheduler.SubscriptionRenewalScheduler;
import com.yowyob.loyalty.domain.subscription.port.in.*;
import com.yowyob.loyalty.domain.subscription.port.out.*;
import com.yowyob.loyalty.domain.subscription.service.SubscriptionDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SubscriptionConfig {

    @Bean
    public SubscriptionDomainService subscriptionDomainService(
            SubscriptionPlanRepository planRepository,
            TenantSubscriptionRepository subscriptionRepository,
            InvoiceRepository invoiceRepository) {
        return new SubscriptionDomainService(planRepository, subscriptionRepository, invoiceRepository);
    }

    @Bean
    public GetPlanUseCase getPlanUseCase(SubscriptionDomainService service) { return service; }

    @Bean
    public ManagePlanUseCase managePlanUseCase(SubscriptionDomainService service) { return service; }

    @Bean
    public SubscribeUseCase subscribeUseCase(SubscriptionDomainService service) { return service; }

    @Bean
    public GetSubscriptionUseCase getSubscriptionUseCase(SubscriptionDomainService service) { return service; }

    @Bean
    public ProcessSubscriptionRenewalUseCase processSubscriptionRenewalUseCase(SubscriptionDomainService service) { return service; }

    @Bean
    public SubscriptionRenewalScheduler subscriptionRenewalScheduler(ProcessSubscriptionRenewalUseCase renewalUseCase) {
        return new SubscriptionRenewalScheduler(renewalUseCase);
    }
}
