package com.yowyob.loyalty.domain.referral.exception;

import java.util.Map;

public class ReferralLinkNotFoundException extends ReferralDomainException {
    public ReferralLinkNotFoundException(String code) {
        super("Lien de parrainage introuvable : " + code, Map.of("code", code));
    }
}
