package com.yowyob.loyalty.infrastructure.persistence.campaign.mapper;

import com.yowyob.loyalty.domain.campaign.model.Campaign;
import com.yowyob.loyalty.domain.campaign.model.CampaignStatus;
import com.yowyob.loyalty.domain.campaign.model.CampaignType;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.persistence.campaign.entity.CampaignEntity;
import org.springframework.stereotype.Component;

@Component
public class CampaignMapper {

    public CampaignEntity toEntity(Campaign domain) {
        CampaignEntity e = new CampaignEntity();
        e.setId(domain.id());
        e.setTenantId(domain.tenantId().value());
        e.setName(domain.name());
        e.setDescription(domain.description());
        e.setCampaignType(domain.campaignType().name());
        e.setTargetEventType(domain.targetEventType());
        e.setBonusMultiplier(domain.bonusMultiplier());
        e.setBonusPoints(domain.bonusPoints());
        e.setStartDate(domain.startDate());
        e.setEndDate(domain.endDate());
        e.setStatus(domain.status().name());
        e.setCreatedAt(domain.createdAt());
        e.setUpdatedAt(domain.updatedAt());
        return e;
    }

    public Campaign toDomain(CampaignEntity e) {
        return Campaign.reconstruct(
                e.getId(),
                TenantId.of(e.getTenantId()),
                e.getName(),
                e.getDescription(),
                CampaignType.valueOf(e.getCampaignType()),
                e.getTargetEventType(),
                e.getBonusMultiplier(),
                e.getBonusPoints(),
                e.getStartDate(),
                e.getEndDate(),
                CampaignStatus.valueOf(e.getStatus()),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
