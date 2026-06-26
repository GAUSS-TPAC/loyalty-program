package com.yowyob.loyalty.domain.referral.exception;

import java.util.Map;
import java.util.UUID;

public class ReferralAlreadyConvertedException extends ReferralDomainException {
    public ReferralAlreadyConvertedException(UUID eventId) {
        super("Ce parrainage a déjà été converti", Map.of("eventId", eventId.toString()));
    }
}
