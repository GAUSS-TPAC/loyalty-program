package com.yowyob.loyaulty.program.shared.exception;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Levée quand le solde disponible d'un wallet est insuffisant pour une opération.
 */
public class InsufficientBalanceException extends RuntimeException {

    private final UUID walletId;
    private final BigDecimal available;
    private final BigDecimal requested;

    public InsufficientBalanceException(UUID walletId, BigDecimal available, BigDecimal requested) {
        super("Solde insuffisant sur le wallet %s : disponible=%s, demandé=%s"
                .formatted(walletId, available.toPlainString(), requested.toPlainString()));
        this.walletId  = walletId;
        this.available = available;
        this.requested = requested;
    }

    public UUID       getWalletId()  { return walletId;  }
    public BigDecimal getAvailable() { return available;  }
    public BigDecimal getRequested() { return requested;  }
}
