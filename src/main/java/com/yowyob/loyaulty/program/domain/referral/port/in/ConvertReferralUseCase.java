package com.yowyob.loyaulty.program.domain.referral.port.in;

import com.yowyob.loyaulty.program.domain.referral.model.ReferralEvent;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface ConvertReferralUseCase {
    /**
     * Appelé quand un filleul réalise l'action qualifiante (premier achat, etc.).
     * Vérifie les conditions, applique les récompenses aux deux parties.
     */
    Mono<ReferralEvent> convert(TenantId tenantId, String refereeId,
                                 String eventType, BigDecimal amount);
}
