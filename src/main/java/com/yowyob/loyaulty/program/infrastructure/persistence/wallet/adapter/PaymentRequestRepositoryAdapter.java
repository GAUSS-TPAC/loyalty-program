package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.adapter;

import com.yowyob.loyaulty.program.domain.wallet.model.PaymentRequest;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.PaymentProvider;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.PaymentRequestStatus;
import com.yowyob.loyaulty.program.domain.wallet.port.out.PaymentRequestRepository;
import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.mapper.PaymentRequestMapper;
import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.repository.PaymentRequestR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Adaptateur infrastructure implémentant {@link PaymentRequestRepository}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestRepositoryAdapter implements PaymentRequestRepository {

    private final PaymentRequestR2dbcRepository r2dbcRepository;
    private final PaymentRequestMapper mapper;

    @Override
    public Mono<PaymentRequest> findById(UUID paymentRequestId, UUID tenantId) {
        return r2dbcRepository.findByIdAndTenantId(paymentRequestId, tenantId)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<PaymentRequest> findByExternalReference(String externalReference,
                                                         PaymentProvider provider) {
        return r2dbcRepository
                .findByExternalReferenceAndProvider(externalReference, provider.name())
                .map(mapper::toDomain)
                .doOnNext(pr -> log.debug("PaymentRequest trouvée par ref externe: ref={}, provider={}",
                        externalReference, provider));
    }

    @Override
    public Mono<PaymentRequest> findByWalletTransactionId(UUID walletTransactionId, UUID tenantId) {
        return r2dbcRepository
                .findByWalletTransactionIdAndTenantId(walletTransactionId, tenantId)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<PaymentRequest> save(PaymentRequest paymentRequest) {
        return r2dbcRepository.save(mapper.toEntity(paymentRequest))
                .map(mapper::toDomain)
                .doOnNext(pr -> log.debug("PaymentRequest sauvegardée : id={}, status={}",
                        pr.getId(), pr.getStatus()));
    }

    @Override
    public Flux<PaymentRequest> findPendingRetryRequests() {
        return r2dbcRepository.findPendingRetryRequests(Instant.now())
                .map(mapper::toDomain);
    }

    @Override
    public Flux<PaymentRequest> findActiveByWalletId(UUID walletId, UUID tenantId) {
        return r2dbcRepository.findActiveByWalletId(walletId, tenantId)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<PaymentRequest> updateStatus(UUID paymentRequestId, UUID tenantId,
                                              PaymentRequestStatus newStatus,
                                              String externalReference,
                                              String providerError) {
        return r2dbcRepository
                .updateStatus(paymentRequestId, tenantId, newStatus.name(),
                        externalReference, providerError)
                .flatMap(rows -> {
                    if (rows == 0) {
                        return Mono.error(new IllegalStateException(
                                "PaymentRequest introuvable : id=" + paymentRequestId));
                    }
                    return findById(paymentRequestId, tenantId);
                })
                .doOnNext(pr -> log.info("PaymentRequest mise à jour : id={}, status={}",
                        paymentRequestId, newStatus));
    }
}
