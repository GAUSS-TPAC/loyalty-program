package com.yowyob.loyaulty.program.domain.member.port.in;

import com.yowyob.loyaulty.program.domain.member.model.PointsAccount;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

public interface EarnPointsUseCase {

    /**
     * Crédite {@code basePoints} au membre en appliquant le multiplicateur de son palier.
     * Retourne le PointsAccount mis à jour.
     */
    Mono<PointsAccount> earn(TenantId tenantId, String memberId,
                              long basePoints, String description, String sourceRef);
}
