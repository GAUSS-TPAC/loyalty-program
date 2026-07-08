package com.yowyob.loyalty.infrastructure.config;

import com.yowyob.loyalty.application.referral.handler.ReferralHandler;
import com.yowyob.loyalty.domain.referral.model.ReferralEvent;
import com.yowyob.loyalty.domain.referral.model.ReferralLink;
import com.yowyob.loyalty.domain.referral.model.ReferralProgram;
import com.yowyob.loyalty.domain.referral.port.in.*;
import com.yowyob.loyalty.domain.referral.port.out.*;
import com.yowyob.loyalty.domain.referral.service.ReferralDomainService;
import com.yowyob.loyalty.domain.reward.port.in.GrantRewardUseCase;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

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

    // ReferralDomainService and ReferralHandler both implement all 5 use-case interfaces
    // below directly, so re-exposing `handler` verbatim under each interface type makes
    // every explicit bean also match every *other* interface (and the raw domain/handler
    // beans too), causing NoUniqueBeanDefinitionException. Single-interface adapters here,
    // plus @Primary, keep exactly one unambiguous candidate per interface type.

    @Bean
    @Primary
    public CreateReferralLinkUseCase createReferralLinkUseCase(ReferralHandler handler) {
        return handler::createLink;
    }

    @Bean
    @Primary
    public RegisterReferralUseCase registerReferralUseCase(ReferralHandler handler) {
        return handler::register;
    }

    @Bean
    @Primary
    public ConvertReferralUseCase convertReferralUseCase(ReferralHandler handler) {
        return handler::convert;
    }

    @Bean
    @Primary
    public GetReferralStatsUseCase getReferralStatsUseCase(ReferralHandler handler) {
        return new GetReferralStatsUseCase() {
            @Override
            public Mono<ReferralLink> getMyLink(TenantId tenantId, UserId referrerId) {
                return handler.getMyLink(tenantId, referrerId);
            }

            @Override
            public Flux<ReferralEvent> getMyReferrals(TenantId tenantId, UserId referrerId) {
                return handler.getMyReferrals(tenantId, referrerId);
            }
        };
    }

    @Bean
    @Primary
    public ManageReferralProgramUseCase manageReferralProgramUseCase(ReferralHandler handler) {
        return new ManageReferralProgramUseCase() {
            @Override
            public Mono<ReferralProgram> createProgram(TenantId tenantId, String name, BigDecimal referrerReward,
                                                        BigDecimal refereeReward, int maxReferrals,
                                                        LocalDate startDate, LocalDate endDate) {
                return handler.createProgram(tenantId, name, referrerReward, refereeReward, maxReferrals, startDate, endDate);
            }

            @Override
            public Mono<ReferralProgram> activateProgram(TenantId tenantId, UUID programId) {
                return handler.activateProgram(tenantId, programId);
            }

            @Override
            public Mono<ReferralProgram> deactivateProgram(TenantId tenantId, UUID programId) {
                return handler.deactivateProgram(tenantId, programId);
            }

            @Override
            public Mono<ReferralProgram> getActiveProgram(TenantId tenantId) {
                return handler.getActiveProgram(tenantId);
            }
        };
    }
}
