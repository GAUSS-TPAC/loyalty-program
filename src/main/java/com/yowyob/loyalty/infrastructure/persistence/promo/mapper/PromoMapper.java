package com.yowyob.loyalty.infrastructure.persistence.promo.mapper;

import com.yowyob.loyalty.domain.promo.model.PromoCampaign;
import com.yowyob.loyalty.domain.promo.model.PromoDiscountType;
import com.yowyob.loyalty.domain.promo.model.PromoUsage;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.infrastructure.persistence.promo.entity.PromoCampaignEntity;
import com.yowyob.loyalty.infrastructure.persistence.promo.entity.PromoUsageEntity;
import org.springframework.stereotype.Component;

@Component
public class PromoMapper {

    public PromoCampaignEntity toEntity(PromoCampaign domain) {
        PromoCampaignEntity e = new PromoCampaignEntity();
        e.setId(domain.id());
        e.setTenantId(domain.tenantId().value());
        e.setCode(domain.code());
        e.setName(domain.name());
        e.setDiscountType(domain.discountType().name());
        e.setDiscountValue(domain.discountValue());
        e.setMinOrderAmount(domain.minOrderAmount());
        e.setMaxUses(domain.maxUses());
        e.setPerMemberLimit(domain.perMemberLimit());
        e.setStartDate(domain.startDate());
        e.setEndDate(domain.endDate());
        e.setActive(domain.isActive());
        e.setCreatedAt(domain.createdAt());
        e.setUpdatedAt(domain.updatedAt());
        return e;
    }

    public PromoCampaign toDomain(PromoCampaignEntity e) {
        return PromoCampaign.reconstruct(
                e.getId(),
                TenantId.of(e.getTenantId()),
                e.getCode(),
                e.getName(),
                PromoDiscountType.valueOf(e.getDiscountType()),
                e.getDiscountValue(),
                e.getMinOrderAmount(),
                e.getMaxUses(),
                e.getPerMemberLimit(),
                e.getStartDate(),
                e.getEndDate(),
                e.isActive(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public PromoUsageEntity toEntity(PromoUsage domain) {
        PromoUsageEntity e = new PromoUsageEntity();
        e.setId(domain.id());
        e.setTenantId(domain.tenantId().value());
        e.setCampaignId(domain.campaignId());
        e.setMemberId(domain.memberId().value());
        e.setOrderId(domain.orderId());
        e.setDiscountApplied(domain.discountApplied());
        e.setUsedAt(domain.usedAt());
        return e;
    }

    public PromoUsage toDomain(PromoUsageEntity e) {
        return PromoUsage.reconstruct(
                e.getId(),
                TenantId.of(e.getTenantId()),
                e.getCampaignId(),
                UserId.of(e.getMemberId()),
                e.getOrderId(),
                e.getDiscountApplied(),
                e.getUsedAt()
        );
    }
}
