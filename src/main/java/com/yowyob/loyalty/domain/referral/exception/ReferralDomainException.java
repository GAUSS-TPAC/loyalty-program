package com.yowyob.loyalty.domain.referral.exception;

import com.yowyob.loyalty.domain.shared.exception.DomainException;

import java.util.Map;

public class ReferralDomainException extends DomainException {
    public ReferralDomainException(String message) { super(message); }
    public ReferralDomainException(String message, Map<String, Object> details) { super(message, details); }
}
