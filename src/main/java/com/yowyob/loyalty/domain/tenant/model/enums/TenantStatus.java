package com.yowyob.loyalty.domain.tenant.model.enums;

public enum TenantStatus {
    ACTIVE,
    SUSPENDED,
    TRIAL,
    PENDING_SETUP;

    public boolean isOperational() {
        return this == ACTIVE || this == TRIAL;
    }
}
