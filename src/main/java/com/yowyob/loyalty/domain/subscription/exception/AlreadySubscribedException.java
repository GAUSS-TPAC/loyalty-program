package com.yowyob.loyalty.domain.subscription.exception;

public class AlreadySubscribedException extends SubscriptionDomainException {
    public AlreadySubscribedException(String tenantId) {
        super("Tenant already has an active subscription: " + tenantId);
    }
}
