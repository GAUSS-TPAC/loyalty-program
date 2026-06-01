package com.yowyob.loyaulty.program.infrastructure.persistence.promo.mapper;

import com.yowyob.loyaulty.program.domain.promo.model.PromoCampaign;
import com.yowyob.loyaulty.program.domain.promo.model.PromoUsage;
import com.yowyob.loyaulty.program.domain.promo.model.enums.DiscountType;
import com.yowyob.loyaulty.program.domain.promo.model.enums.PromoStatus;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.infrastructure.persistence.promo.entity.PromoCampaignEntity;
import com.yowyob.loyaulty.program.infrastructure.persistence.promo.entity.PromoUsageEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PromoMapper {

    public PromoCampaign toDomain(PromoCampaignEntity e) {
        return PromoCampaign.reconstitute(
                e.getId(), TenantId.of(e.getTenantId()),
                e.getName(), e.getCode(),
                DiscountType.valueOf(e.getDiscountType()),
                e.getDiscountValue(),
                PromoStatus.valueOf(e.getStatus()),
                e.getMaxUsesTotal(), e.getMaxUsesPerMember(),
                e.getMinOrderAmount(), e.getValidFrom(), e.getValidUntil(),
                e.getCreatedAt()
        );
    }

    public PromoCampaignEntity toEntity(PromoCampaign c) {
        PromoCampaignEntity e = new PromoCampaignEntity();
        e.setId(c.getId());
        e.setTenantId(c.getTenantId().value());
        e.setName(c.getName());
        e.setCode(c.getCode());
        e.setDiscountType(c.getDiscountType().name());
        e.setDiscountValue(c.getDiscountValue());
        e.setStatus(c.getStatus().name());
        e.setMaxUsesTotal(c.getMaxUsesTotal());
        e.setMaxUsesPerMember(c.getMaxUsesPerMember());
        e.setMinOrderAmount(c.getMinOrderAmount());
        e.setValidFrom(c.getValidFrom());
        e.setValidUntil(c.getValidUntil());
        e.setCreatedAt(c.getCreatedAt() != null ? c.getCreatedAt() : Instant.now());
        return e;
    }

    public PromoUsage toDomain(PromoUsageEntity e) {
        return PromoUsage.reconstitute(
                e.getId(), TenantId.of(e.getTenantId()),
                e.getCampaignId(), e.getMemberId(),
                e.getOrderReference(), e.getDiscountApplied(), e.getUsedAt()
        );
    }

    public PromoUsageEntity toEntity(PromoUsage u) {
        PromoUsageEntity e = new PromoUsageEntity();
        e.setId(u.getId());
        e.setTenantId(u.getTenantId().value());
        e.setCampaignId(u.getCampaignId());
        e.setMemberId(u.getMemberId());
        e.setOrderReference(u.getOrderReference());
        e.setDiscountApplied(u.getDiscountApplied());
        e.setUsedAt(u.getUsedAt());
        return e;
    }
}
