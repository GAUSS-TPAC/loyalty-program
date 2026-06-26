package com.yowyob.loyalty.domain.subscription.exception;

import java.util.UUID;

public class SubscriptionAlreadyTerminalException extends SubscriptionDomainException {
    public SubscriptionAlreadyTerminalException(UUID subscriptionId) {
        super("Subscription is already cancelled or expired: " + subscriptionId);
    }
}
