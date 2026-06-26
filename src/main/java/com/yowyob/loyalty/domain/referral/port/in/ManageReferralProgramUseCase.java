package com.yowyob.loyalty.domain.referral.port.in;

import com.yowyob.loyalty.domain.referral.model.ReferralProgram;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ManageReferralProgramUseCase {
    Mono<ReferralProgram> createProgram(TenantId tenantId, String name, BigDecimal referrerReward,
                                        BigDecimal refereeReward, int maxReferrals, LocalDate startDate, LocalDate endDate);
    Mono<ReferralProgram> activateProgram(TenantId tenantId, java.util.UUID programId);
    Mono<ReferralProgram> deactivateProgram(TenantId tenantId, java.util.UUID programId);
    Mono<ReferralProgram> getActiveProgram(TenantId tenantId);
}
