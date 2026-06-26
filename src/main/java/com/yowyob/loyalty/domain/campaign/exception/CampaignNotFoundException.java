package com.yowyob.loyalty.domain.campaign.exception;

import java.util.UUID;

public class CampaignNotFoundException extends CampaignDomainException {
    public CampaignNotFoundException(UUID id) {
        super("Campagne introuvable: " + id);
    }
}
