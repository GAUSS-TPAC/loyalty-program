package com.yowyob.loyaulty.program.infrastructure.persistence.promo.repository;

import com.yowyob.loyaulty.program.infrastructure.persistence.promo.entity.PromoUsageEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PromoUsageR2dbcRepository extends R2dbcRepository<PromoUsageEntity, UUID> {

    @Query("SELECT COUNT(*) FROM promo_usages WHERE campaign_id = :campaignId AND tenant_id = :tenantId")
    Mono<Long> countByCampaignIdAndTenantId(UUID campaignId, UUID tenantId);

    @Query("SELECT COUNT(*) FROM promo_usages WHERE campaign_id = :campaignId AND member_id = :memberId AND tenant_id = :tenantId")
    Mono<Long> countByCampaignIdAndMemberIdAndTenantId(UUID campaignId, String memberId, UUID tenantId);
}
