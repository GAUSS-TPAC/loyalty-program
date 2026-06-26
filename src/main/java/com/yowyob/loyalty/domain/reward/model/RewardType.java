package com.yowyob.loyalty.domain.reward.model;

public enum RewardType {
    FREE_PRODUCT,
    PERCENT_DISCOUNT,
    FIXED_DISCOUNT,
    CASHBACK_WALLET,
    EXCLUSIVE_ACCESS,
    TIER_UPGRADE;

    public boolean hasMonetaryValue() {
        return this == PERCENT_DISCOUNT || this == FIXED_DISCOUNT || this == CASHBACK_WALLET;
    }

    public boolean requiresProductId() {
        return this == FREE_PRODUCT;
    }
}
