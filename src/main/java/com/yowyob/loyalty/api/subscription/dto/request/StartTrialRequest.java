package com.yowyob.loyalty.api.subscription.dto.request;

import java.util.UUID;

public record StartTrialRequest(
        UUID planId,
        int trialDays
) {}
