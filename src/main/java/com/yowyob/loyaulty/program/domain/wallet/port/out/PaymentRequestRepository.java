package com.yowyob.loyaulty.program.domain.wallet.port.out;

import com.yowyob.loyaulty.program.domain.wallet.model.PaymentRequest;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentRequestRepository {
    Mono<PaymentRequest> save(PaymentRequest paymentRequest);
    Mono<PaymentRequest> findById(UUID id);
    Mono<PaymentRequest> findByExternalRef(String externalRef);
    Mono<PaymentRequest> findByWalletTransactionId(UUID walletTransactionId);
}
