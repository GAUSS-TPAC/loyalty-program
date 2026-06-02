package com.yowyob.loyalty.domain.wallet.model;

public enum TransactionType {
    CREDIT,
    DEBIT,
    REVERSAL,
    RESERVE,
    RELEASE;

    public boolean isCredit() {
        return this == CREDIT || this == RELEASE;
    }

    public boolean isDebit() {
        return this == DEBIT || this == RESERVE;
    }
}
