package com.yowyob.loyalty.domain.referral.exception;

public class SelfReferralException extends ReferralDomainException {
    public SelfReferralException() {
        super("Un membre ne peut pas se parrainer lui-même");
    }
}
