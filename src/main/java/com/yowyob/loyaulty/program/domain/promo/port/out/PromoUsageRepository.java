package com.yowyob.loyaulty.program.domain.promo.port.out;

import com.yowyob.loyaulty.program.domain.promo.model.PromoUsage;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PromoUsageRepository {

    Mono<PromoUsage> save(PromoUsage usage);

    Mono<Long> countByCampaign(UUID campaignId, TenantId tenantId);

    Mono<Long> countByCampaignAndMember(UUID campaignId, String memberId, TenantId tenantId);
}
