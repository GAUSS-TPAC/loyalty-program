package com.yowyob.loyalty.api.subscription.dto.request;

import java.util.UUID;

public record SubscribeRequest(
        UUID planId,
        String billingCycle
) {}
