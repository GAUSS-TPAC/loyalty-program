package com.yowyob.loyalty.domain.wallet.model;

public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REVERSED;

    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == REVERSED;
    }
}
