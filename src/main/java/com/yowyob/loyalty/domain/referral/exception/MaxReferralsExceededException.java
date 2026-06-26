package com.yowyob.loyalty.domain.referral.exception;

import java.util.Map;

public class MaxReferralsExceededException extends ReferralDomainException {
    public MaxReferralsExceededException(int max) {
        super("Nombre maximum de parrainages atteint", Map.of("max", max));
    }
}
