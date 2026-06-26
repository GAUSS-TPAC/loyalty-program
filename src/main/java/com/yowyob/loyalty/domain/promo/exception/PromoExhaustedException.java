package com.yowyob.loyalty.domain.promo.exception;

public class PromoExhaustedException extends PromoDomainException {
    public PromoExhaustedException(String code) {
        super("Le code promo a atteint son nombre maximum d'utilisations: " + code);
    }
}
