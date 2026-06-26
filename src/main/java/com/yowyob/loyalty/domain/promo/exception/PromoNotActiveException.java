package com.yowyob.loyalty.domain.promo.exception;

public class PromoNotActiveException extends PromoDomainException {
    public PromoNotActiveException(String code) {
        super("Le code promo n'est pas actif: " + code);
    }
}
