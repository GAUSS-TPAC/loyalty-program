package com.yowyob.loyalty.api.campaign.dto.request;

import com.yowyob.loyalty.domain.campaign.model.CampaignType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateCampaignRequest(
        @NotBlank String name,
        String description,
        @NotNull CampaignType campaignType,
        String targetEventType,
        BigDecimal bonusMultiplier,
        long bonusPoints,
        @NotNull Instant startDate,
        Instant endDate
) {}
