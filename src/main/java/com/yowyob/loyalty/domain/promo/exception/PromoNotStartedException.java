package com.yowyob.loyalty.domain.promo.exception;

public class PromoNotStartedException extends PromoDomainException {
    public PromoNotStartedException(String code) {
        super("Le code promo n'est pas encore disponible: " + code);
    }
}
