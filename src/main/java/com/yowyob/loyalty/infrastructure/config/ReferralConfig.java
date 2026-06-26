package com.yowyob.loyalty.infrastructure.config;

import com.yowyob.loyalty.application.referral.handler.ReferralHandler;
import com.yowyob.loyalty.domain.referral.port.in.*;
import com.yowyob.loyalty.domain.referral.port.out.*;
import com.yowyob.loyalty.domain.referral.service.ReferralDomainService;
import com.yowyob.loyalty.domain.reward.port.in.GrantRewardUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReferralConfig {

    @Bean
    public ReferralDomainService referralDomainService(
            ReferralProgramRepository programRepository,
            ReferralLinkRepository linkRepository,
            ReferralEventRepository eventRepository,
            ReferralEventPublisherPort publisher,
            GrantRewardUseCase grantRewardUseCase) {
        return new ReferralDomainService(programRepository, linkRepository, eventRepository, publisher, grantRewardUseCase);
    }

    @Bean
    public CreateReferralLinkUseCase createReferralLinkUseCase(ReferralHandler handler) {
        return handler;
    }

    @Bean
    public RegisterReferralUseCase registerReferralUseCase(ReferralHandler handler) {
        return handler;
    }

    @Bean
    public ConvertReferralUseCase convertReferralUseCase(ReferralHandler handler) {
        return handler;
    }

    @Bean
    public GetReferralStatsUseCase getReferralStatsUseCase(ReferralHandler handler) {
        return handler;
    }

    @Bean
    public ManageReferralProgramUseCase manageReferralProgramUseCase(ReferralHandler handler) {
        return handler;
    }
}
