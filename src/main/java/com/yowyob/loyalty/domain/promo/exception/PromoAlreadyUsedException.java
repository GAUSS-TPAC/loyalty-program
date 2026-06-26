package com.yowyob.loyalty.domain.promo.exception;

public class PromoAlreadyUsedException extends PromoDomainException {
    public PromoAlreadyUsedException(String code) {
        super("Le code promo a déjà été utilisé par ce membre: " + code);
    }
}
