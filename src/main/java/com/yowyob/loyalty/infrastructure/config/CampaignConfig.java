package com.yowyob.loyalty.infrastructure.config;

import com.yowyob.loyalty.domain.campaign.port.out.CampaignRepository;
import com.yowyob.loyalty.domain.campaign.service.CampaignDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class CampaignConfig {

    @Bean
    public CampaignDomainService campaignDomainService(CampaignRepository campaignRepository) {
        return new CampaignDomainService(campaignRepository);
    }
}
