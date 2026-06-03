package com.yowyob.loyalty.domain.wallet.exception;

import com.yowyob.loyalty.domain.shared.exception.DomainException;
import java.util.Map;

public class WalletDomainException extends DomainException {
    
    public WalletDomainException(String message) {
        super(message);
    }

    public WalletDomainException(String message, Map<String, Object> details) {
        super(message);
        // On suppose que DomainException gère les détails ou on peut les ignorer si non supportés par la classe parente
    }
}
