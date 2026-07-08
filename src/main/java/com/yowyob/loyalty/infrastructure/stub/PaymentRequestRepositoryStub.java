package com.yowyob.loyalty.infrastructure.stub;

import com.yowyob.loyalty.domain.wallet.model.PaymentRequest;
import com.yowyob.loyalty.domain.wallet.port.out.PaymentRequestRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// No R2DBC adapter exists yet for PaymentRequestRepository (withdrawal/payment-provider
// flow is not implemented). This in-memory stub only keeps the app wireable in dev/test.
@Component
@Profile({"test", "stub", "dev"})
public class PaymentRequestRepositoryStub implements PaymentRequestRepository {

    private final Map<UUID, PaymentRequest> byId = new ConcurrentHashMap<>();
    private final Map<String, PaymentRequest> byExternalRef = new ConcurrentHashMap<>();

    @Override
    public Mono<PaymentRequest> save(PaymentRequest request) {
        byId.put(request.id(), request);
        byExternalRef.put(request.externalRef(), request);
        return Mono.just(request);
    }

    @Override
    public Mono<PaymentRequest> findByExternalRef(String externalRef) {
        return Mono.justOrEmpty(byExternalRef.get(externalRef));
    }

    @Override
    public Mono<PaymentRequest> findById(UUID id) {
        return Mono.justOrEmpty(byId.get(id));
    }

    @Override
    public Mono<PaymentRequest> update(PaymentRequest request) {
        return save(request);
    }
}
