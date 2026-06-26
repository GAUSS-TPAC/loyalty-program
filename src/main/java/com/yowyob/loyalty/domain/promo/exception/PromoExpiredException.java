package com.yowyob.loyalty.domain.promo.exception;

public class PromoExpiredException extends PromoDomainException {
    public PromoExpiredException(String code) {
        super("Le code promo a expiré: " + code);
    }
}
