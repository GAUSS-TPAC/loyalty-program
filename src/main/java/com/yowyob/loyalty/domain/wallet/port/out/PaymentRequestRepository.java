package com.yowyob.loyalty.domain.wallet.port.out;

import com.yowyob.loyalty.domain.wallet.model.PaymentRequest;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface PaymentRequestRepository {
    Mono<PaymentRequest> save(PaymentRequest request);
    Mono<PaymentRequest> findByExternalRef(String externalRef);
    Mono<PaymentRequest> findById(UUID id);
    Mono<PaymentRequest> update(PaymentRequest request);
}
