package com.yowyob.loyalty.domain.promo.exception;

import java.math.BigDecimal;

public class PromoMinOrderAmountException extends PromoDomainException {
    public PromoMinOrderAmountException(BigDecimal minAmount, BigDecimal actual) {
        super("Montant minimum requis: " + minAmount + ", montant de la commande: " + actual);
    }
}
