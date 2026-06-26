package com.yowyob.loyalty.domain.subscription.exception;

import java.util.UUID;

public class SubscriptionNotFoundException extends SubscriptionDomainException {
    public SubscriptionNotFoundException(UUID id) {
        super("Tenant subscription not found: " + id);
    }
    public SubscriptionNotFoundException(String tenantId) {
        super("No active subscription for tenant: " + tenantId);
    }
}
