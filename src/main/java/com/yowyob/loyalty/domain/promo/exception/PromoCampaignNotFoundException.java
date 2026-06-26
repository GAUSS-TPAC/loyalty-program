package com.yowyob.loyalty.domain.promo.exception;

public class PromoCampaignNotFoundException extends PromoDomainException {
    public PromoCampaignNotFoundException(String code) {
        super("Campagne promo introuvable: " + code);
    }
}
