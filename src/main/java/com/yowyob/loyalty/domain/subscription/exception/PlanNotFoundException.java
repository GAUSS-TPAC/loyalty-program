package com.yowyob.loyalty.domain.subscription.exception;

import java.util.UUID;

public class PlanNotFoundException extends SubscriptionDomainException {
    public PlanNotFoundException(UUID id) {
        super("Subscription plan not found: " + id);
    }
    public PlanNotFoundException(String code) {
        super("Subscription plan not found with code: " + code);
    }
}
