package com.yowyob.loyalty.domain.reward.exception;

import com.yowyob.loyalty.domain.shared.exception.DomainException;

import java.util.Map;

public class RewardDomainException extends DomainException {
    public RewardDomainException(String message) { super(message); }
    public RewardDomainException(String message, Map<String, Object> details) { super(message, details); }
}
