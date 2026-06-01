package com.yowyob.loyaulty.program.shared.exception;

import java.util.UUID;

/**
 * Levée quand une opération de débit/retrait est tentée sur un wallet gelé.
 */
public class WalletFrozenException extends RuntimeException {

    private final UUID walletId;
    private final String freezeReason;

    public WalletFrozenException(UUID walletId, String freezeReason) {
        super("Le wallet %s est gelé. Motif : %s".formatted(walletId, freezeReason));
        this.walletId     = walletId;
        this.freezeReason = freezeReason;
    }

    public UUID   getWalletId()     { return walletId;     }
    public String getFreezeReason() { return freezeReason; }
}
