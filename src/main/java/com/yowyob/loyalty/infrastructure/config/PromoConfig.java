package com.yowyob.loyalty.infrastructure.config;

import com.yowyob.loyalty.domain.promo.model.PromoCampaign;
import com.yowyob.loyalty.domain.promo.port.in.*;
import com.yowyob.loyalty.domain.promo.port.out.*;
import com.yowyob.loyalty.domain.promo.service.PromoDomainService;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Configuration
public class PromoConfig {

    @Bean
    public PromoDomainService promoDomainService(
            PromoCampaignRepository campaignRepository,
            PromoUsageRepository usageRepository,
            PromoUsageCounterPort usageCounter) {
        return new PromoDomainService(campaignRepository, usageRepository, usageCounter);
    }

    // PromoDomainService implements all 5 use-case interfaces directly, so exposing it
    // verbatim under each interface type (as the other Config classes in this codebase do)
    // makes every explicit bean below also match every *other* interface, causing
    // NoUniqueBeanDefinitionException as soon as something is wired by one of these interfaces.
    // Wrapping in single-interface adapters here, plus @Primary over the raw promoDomainService
    // bean, keeps exactly one unambiguous candidate per interface type.

    @Bean
    @Primary
    public CreatePromoCampaignUseCase createPromoCampaignUseCase(PromoDomainService service) {
        return service::createCampaign;
    }

    @Bean
    @Primary
    public ValidatePromoCodeUseCase validatePromoCodeUseCase(PromoDomainService service) {
        return service::validate;
    }

    @Bean
    @Primary
    public ApplyPromoCodeUseCase applyPromoCodeUseCase(PromoDomainService service) {
        return service::apply;
    }

    @Bean
    @Primary
    public GetPromoCampaignUseCase getPromoCampaignUseCase(PromoDomainService service) {
        return new GetPromoCampaignUseCase() {
            @Override
            public Mono<PromoCampaign> getById(TenantId tenantId, UUID campaignId) {
                return service.getById(tenantId, campaignId);
            }

            @Override
            public Mono<PromoCampaign> getByCode(TenantId tenantId, String code) {
                return service.getByCode(tenantId, code);
            }

            @Override
            public Flux<PromoCampaign> listAll(TenantId tenantId) {
                return service.listAll(tenantId);
            }

            @Override
            public Flux<PromoCampaign> listActive(TenantId tenantId) {
                return service.listActive(tenantId);
            }
        };
    }

    @Bean
    @Primary
    public ManagePromoCampaignUseCase managePromoCampaignUseCase(PromoDomainService service) {
        return new ManagePromoCampaignUseCase() {
            @Override
            public Mono<PromoCampaign> activate(TenantId tenantId, UUID campaignId) {
                return service.activate(tenantId, campaignId);
            }

            @Override
            public Mono<PromoCampaign> deactivate(TenantId tenantId, UUID campaignId) {
                return service.deactivate(tenantId, campaignId);
            }

            @Override
            public Mono<Void> delete(TenantId tenantId, UUID campaignId) {
                return service.delete(tenantId, campaignId);
            }
        };
    }
}
