package com.yowyob.loyalty.api.campaign.dto.response;

import com.yowyob.loyalty.domain.campaign.model.Campaign;
import com.yowyob.loyalty.domain.campaign.model.CampaignStatus;
import com.yowyob.loyalty.domain.campaign.model.CampaignType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CampaignResponse(
        UUID id,
        String name,
        String description,
        CampaignType campaignType,
        String targetEventType,
        BigDecimal bonusMultiplier,
        long bonusPoints,
        Instant startDate,
        Instant endDate,
        CampaignStatus status,
        Instant createdAt
) {
    public static CampaignResponse from(Campaign c) {
        return new CampaignResponse(
                c.id(), c.name(), c.description(), c.campaignType(),
                c.targetEventType(), c.bonusMultiplier(), c.bonusPoints(),
                c.startDate(), c.endDate(), c.status(), c.createdAt()
        );
    }
}
