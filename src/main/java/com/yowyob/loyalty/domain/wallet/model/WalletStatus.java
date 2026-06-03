package com.yowyob.loyalty.domain.wallet.model;

/**
 * Enum Java pur représentant le cycle de vie d'un wallet.
 */
public enum WalletStatus {
    PENDING_KYC,
    ACTIVE,
    FROZEN,
    CLOSED;

    public boolean canDebit() {
        return this == ACTIVE;
    }

    public boolean canCredit() {
        return this == ACTIVE || this == PENDING_KYC;
    }

    public boolean isFinal() {
        return this == CLOSED;
    }

    public boolean canTransitionTo(WalletStatus next) {
        if (next == null) return false;
        
        return switch (this) {
            case PENDING_KYC -> next == ACTIVE || next == CLOSED;
            case ACTIVE -> next == FROZEN || next == CLOSED;
            case FROZEN -> next == ACTIVE || next == CLOSED;
            case CLOSED -> false;
        };
    }
}
