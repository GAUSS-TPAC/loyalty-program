package com.yowyob.loyalty.application.referral.handler;

import com.yowyob.loyalty.domain.referral.model.ReferralEvent;
import com.yowyob.loyalty.domain.referral.model.ReferralLink;
import com.yowyob.loyalty.domain.referral.model.ReferralProgram;
import com.yowyob.loyalty.domain.referral.port.in.*;
import com.yowyob.loyalty.domain.referral.service.ReferralDomainService;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class ReferralHandler implements
        CreateReferralLinkUseCase,
        RegisterReferralUseCase,
        ConvertReferralUseCase,
        GetReferralStatsUseCase,
        ManageReferralProgramUseCase {

    private final ReferralDomainService service;

    public ReferralHandler(ReferralDomainService service) {
        this.service = service;
    }

    @Override
    public Mono<ReferralLink> createLink(TenantId tenantId, UserId referrerId) {
        return service.createLink(tenantId, referrerId);
    }

    @Override
    public Mono<ReferralEvent> register(TenantId tenantId, String referralCode, UserId refereeId) {
        return service.register(tenantId, referralCode, refereeId);
    }

    @Override
    public Mono<ReferralEvent> convert(TenantId tenantId, UserId refereeId, BigDecimal conversionAmount) {
        return service.convert(tenantId, refereeId, conversionAmount);
    }

    @Override
    public Mono<ReferralLink> getMyLink(TenantId tenantId, UserId referrerId) {
        return service.getMyLink(tenantId, referrerId);
    }

    @Override
    public Flux<ReferralEvent> getMyReferrals(TenantId tenantId, UserId referrerId) {
        return service.getMyReferrals(tenantId, referrerId);
    }

    @Override
    public Mono<ReferralProgram> createProgram(TenantId tenantId, String name, BigDecimal referrerReward,
                                                BigDecimal refereeReward, int maxReferrals,
                                                LocalDate startDate, LocalDate endDate) {
        return service.createProgram(tenantId, name, referrerReward, refereeReward, maxReferrals, startDate, endDate);
    }

    @Override
    public Mono<ReferralProgram> activateProgram(TenantId tenantId, UUID programId) {
        return service.activateProgram(tenantId, programId);
    }

    @Override
    public Mono<ReferralProgram> deactivateProgram(TenantId tenantId, UUID programId) {
        return service.deactivateProgram(tenantId, programId);
    }

    @Override
    public Mono<ReferralProgram> getActiveProgram(TenantId tenantId) {
        return service.getActiveProgram(tenantId);
    }
}
