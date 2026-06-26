package com.yowyob.loyalty.domain.promo.model;

import java.math.BigDecimal;
import java.util.UUID;

public class PromoValidationResult {

    private final boolean valid;
    private final UUID campaignId;
    private final String campaignName;
    private final PromoDiscountType discountType;
    private final BigDecimal discountValue;
    private final BigDecimal calculatedDiscount;
    private final String message;

    private PromoValidationResult(boolean valid, UUID campaignId, String campaignName,
                                   PromoDiscountType discountType, BigDecimal discountValue,
                                   BigDecimal calculatedDiscount, String message) {
        this.valid = valid;
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.calculatedDiscount = calculatedDiscount;
        this.message = message;
    }

    public static PromoValidationResult valid(PromoCampaign campaign, BigDecimal orderAmount) {
        BigDecimal discount = campaign.calculateDiscount(orderAmount);
        return new PromoValidationResult(true, campaign.id(), campaign.name(),
                campaign.discountType(), campaign.discountValue(), discount, "Code promo valide");
    }

    public boolean isValid() { return valid; }
    public UUID campaignId() { return campaignId; }
    public String campaignName() { return campaignName; }
    public PromoDiscountType discountType() { return discountType; }
    public BigDecimal discountValue() { return discountValue; }
    public BigDecimal calculatedDiscount() { return calculatedDiscount; }
    public String message() { return message; }
}
