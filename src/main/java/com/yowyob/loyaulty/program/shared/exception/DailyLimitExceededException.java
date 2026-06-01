package com.yowyob.loyaulty.program.shared.exception;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Levée quand le plafond de dépenses journalières d'un wallet est atteint.
 */
public class DailyLimitExceededException extends RuntimeException {

    private final UUID walletId;
    private final BigDecimal limit;
    private final BigDecimal alreadySpent;
    private final BigDecimal requested;

    public DailyLimitExceededException(UUID walletId, BigDecimal limit,
                                        BigDecimal alreadySpent, BigDecimal requested) {
        super(("Plafond journalier dépassé sur le wallet %s : " +
               "limite=%s, déjà dépensé=%s, demandé=%s")
                .formatted(walletId, limit.toPlainString(),
                        alreadySpent.toPlainString(), requested.toPlainString()));
        this.walletId     = walletId;
        this.limit        = limit;
        this.alreadySpent = alreadySpent;
        this.requested    = requested;
    }

    public UUID       getWalletId()     { return walletId;     }
    public BigDecimal getLimit()        { return limit;        }
    public BigDecimal getAlreadySpent() { return alreadySpent; }
    public BigDecimal getRequested()    { return requested;    }
}
