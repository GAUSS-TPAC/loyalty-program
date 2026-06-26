package com.yowyob.loyalty.domain.loyalty.port.out;

import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ActiveCampaignPort {

    List<CampaignBonus> getActiveBonuses(TenantId tenantId, String eventType);

    record CampaignBonus(UUID campaignId, String campaignName, BonusType type, BigDecimal multiplier, long flatPoints) {
        public long calculateExtraPoints(long earnedPoints) {
            return switch (type) {
                case MULTIPLIER -> (long) (earnedPoints * (multiplier.doubleValue() - 1));
                case FLAT -> flatPoints;
            };
        }
    }

    enum BonusType { MULTIPLIER, FLAT }
}
