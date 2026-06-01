package com.yowyob.loyaulty.program.domain.wallet.model;

import com.yowyob.loyaulty.program.domain.wallet.model.enums.PaymentProvider;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.PaymentRequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PaymentRequest {

    private final UUID id;
    private final UUID walletTransactionId;
    private final PaymentProvider provider;
    private String externalRef;
    private PaymentRequestStatus status;
    private final BigDecimal realAmount;
    private final String realCurrency;
    private final BigDecimal virtualAmount;
    private final BigDecimal exchangeRate;
    private String rawResponse;
    private final Instant initiatedAt;
    private Instant confirmedAt;

    private PaymentRequest(UUID id, UUID walletTransactionId, PaymentProvider provider,
                            BigDecimal realAmount, String realCurrency,
                            BigDecimal virtualAmount, BigDecimal exchangeRate) {
        this.id = id;
        this.walletTransactionId = walletTransactionId;
        this.provider = provider;
        this.realAmount = realAmount;
        this.realCurrency = realCurrency;
        this.virtualAmount = virtualAmount;
        this.exchangeRate = exchangeRate;
        this.status = PaymentRequestStatus.INITIATED;
        this.initiatedAt = Instant.now();
    }

    public static PaymentRequest create(UUID walletTransactionId, PaymentProvider provider,
                                         BigDecimal realAmount, String realCurrency,
                                         BigDecimal virtualAmount, BigDecimal exchangeRate) {
        return new PaymentRequest(
                UUID.randomUUID(), walletTransactionId, provider,
                realAmount, realCurrency, virtualAmount, exchangeRate
        );
    }

    public void markPending(String externalRef) {
        this.externalRef = externalRef;
        this.status = PaymentRequestStatus.PENDING;
    }

    public void confirm(String rawResponse) {
        this.rawResponse = rawResponse;
        this.status = PaymentRequestStatus.CONFIRMED;
        this.confirmedAt = Instant.now();
    }

    public void fail(String rawResponse) {
        this.rawResponse = rawResponse;
        this.status = PaymentRequestStatus.FAILED;
    }

    public void cancel() {
        this.status = PaymentRequestStatus.CANCELLED;
    }

    public boolean isConfirmed() { return status == PaymentRequestStatus.CONFIRMED; }
    public boolean isFailed() { return status == PaymentRequestStatus.FAILED; }

    public UUID getId() { return id; }
    public UUID getWalletTransactionId() { return walletTransactionId; }
    public PaymentProvider getProvider() { return provider; }
    public String getExternalRef() { return externalRef; }
    public PaymentRequestStatus getStatus() { return status; }
    public BigDecimal getRealAmount() { return realAmount; }
    public String getRealCurrency() { return realCurrency; }
    public BigDecimal getVirtualAmount() { return virtualAmount; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public String getRawResponse() { return rawResponse; }
    public Instant getInitiatedAt() { return initiatedAt; }
    public Instant getConfirmedAt() { return confirmedAt; }
}
