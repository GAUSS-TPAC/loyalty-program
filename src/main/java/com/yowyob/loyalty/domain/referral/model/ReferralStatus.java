package com.yowyob.loyalty.domain.referral.model;

public enum ReferralStatus {
    PENDING,
    ENROLLED,
    CONVERTED,
    FRAUD,
    EXPIRED;

    public boolean isFinal() {
        return this == CONVERTED || this == FRAUD || this == EXPIRED;
    }
}
