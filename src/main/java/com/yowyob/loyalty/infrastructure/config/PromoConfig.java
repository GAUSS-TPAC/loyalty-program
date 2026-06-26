package com.yowyob.loyalty.infrastructure.config;

import com.yowyob.loyalty.domain.promo.port.in.*;
import com.yowyob.loyalty.domain.promo.port.out.*;
import com.yowyob.loyalty.domain.promo.service.PromoDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PromoConfig {

    @Bean
    public PromoDomainService promoDomainService(
            PromoCampaignRepository campaignRepository,
            PromoUsageRepository usageRepository,
            PromoUsageCounterPort usageCounter) {
        return new PromoDomainService(campaignRepository, usageRepository, usageCounter);
    }

    @Bean
    public CreatePromoCampaignUseCase createPromoCampaignUseCase(PromoDomainService service) {
        return service;
    }

    @Bean
    public ValidatePromoCodeUseCase validatePromoCodeUseCase(PromoDomainService service) {
        return service;
    }

    @Bean
    public ApplyPromoCodeUseCase applyPromoCodeUseCase(PromoDomainService service) {
        return service;
    }

    @Bean
    public GetPromoCampaignUseCase getPromoCampaignUseCase(PromoDomainService service) {
        return service;
    }

    @Bean
    public ManagePromoCampaignUseCase managePromoCampaignUseCase(PromoDomainService service) {
        return service;
    }
}
