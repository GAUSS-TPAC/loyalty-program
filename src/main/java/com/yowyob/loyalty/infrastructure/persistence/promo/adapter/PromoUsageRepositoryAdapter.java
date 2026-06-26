package com.yowyob.loyalty.infrastructure.persistence.promo.adapter;

import com.yowyob.loyalty.domain.promo.model.PromoUsage;
import com.yowyob.loyalty.domain.promo.port.out.PromoUsageRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.infrastructure.persistence.promo.mapper.PromoMapper;
import com.yowyob.loyalty.infrastructure.persistence.promo.repository.PromoUsageR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class PromoUsageRepositoryAdapter implements PromoUsageRepository {

    private final PromoUsageR2dbcRepository r2dbcRepo;
    private final PromoMapper mapper;

    public PromoUsageRepositoryAdapter(PromoUsageR2dbcRepository r2dbcRepo, PromoMapper mapper) {
        this.r2dbcRepo = r2dbcRepo;
        this.mapper = mapper;
    }

    @Override
    public Mono<PromoUsage> save(PromoUsage usage) {
        return r2dbcRepo.save(mapper.toEntity(usage)).map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countByCampaignId(TenantId tenantId, UUID campaignId) {
        return r2dbcRepo.countByTenantIdAndCampaignId(tenantId.value(), campaignId);
    }

    @Override
    public Mono<Long> countByMemberAndCampaign(TenantId tenantId, UUID campaignId, UserId memberId) {
        return r2dbcRepo.countByTenantIdAndCampaignIdAndMemberId(
                tenantId.value(), campaignId, memberId.value());
    }

    @Override
    public Mono<Boolean> existsByOrderId(TenantId tenantId, UUID campaignId, String orderId) {
        return r2dbcRepo.existsByTenantIdAndCampaignIdAndOrderId(tenantId.value(), campaignId, orderId);
    }

    @Override
    public Flux<PromoUsage> findByMember(TenantId tenantId, UserId memberId) {
        return r2dbcRepo.findAllByTenantIdAndMemberId(
                tenantId.value(), memberId.value()).map(mapper::toDomain);
    }
}
