package com.yowyob.loyalty.infrastructure.persistence.campaign.adapter;

import com.yowyob.loyalty.domain.campaign.model.Campaign;
import com.yowyob.loyalty.domain.campaign.model.CampaignStatus;
import com.yowyob.loyalty.domain.campaign.model.CampaignType;
import com.yowyob.loyalty.domain.loyalty.port.out.ActiveCampaignPort;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.persistence.campaign.mapper.CampaignMapper;
import com.yowyob.loyalty.infrastructure.persistence.campaign.repository.CampaignR2dbcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ActiveCampaignPortAdapter implements ActiveCampaignPort {

    private static final Logger log = LoggerFactory.getLogger(ActiveCampaignPortAdapter.class);

    private final CampaignR2dbcRepository r2dbcRepo;
    private final CampaignMapper mapper;

    public ActiveCampaignPortAdapter(CampaignR2dbcRepository r2dbcRepo, CampaignMapper mapper) {
        this.r2dbcRepo = r2dbcRepo;
        this.mapper = mapper;
    }

    @Override
    public List<CampaignBonus> getActiveBonuses(TenantId tenantId, String eventType) {
        try {
            return r2dbcRepo.findAllByTenantIdAndStatus(tenantId.value(), CampaignStatus.ACTIVE.name())
                    .map(mapper::toDomain)
                    .filter(c -> c.appliesToEvent(eventType))
                    .map(this::toBonus)
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.warn("Failed to load active campaign bonuses: {}", e.getMessage());
            return List.of();
        }
    }

    private CampaignBonus toBonus(Campaign c) {
        BonusType type = c.campaignType() == CampaignType.BONUS_MULTIPLIER ? BonusType.MULTIPLIER : BonusType.FLAT;
        return new CampaignBonus(c.id(), c.name(), type, c.bonusMultiplier(), c.bonusPoints());
    }
}
