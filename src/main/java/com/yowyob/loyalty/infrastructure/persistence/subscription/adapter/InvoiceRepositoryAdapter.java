package com.yowyob.loyalty.infrastructure.persistence.subscription.adapter;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.subscription.model.InvoiceRecord;
import com.yowyob.loyalty.domain.subscription.port.out.InvoiceRepository;
import com.yowyob.loyalty.infrastructure.persistence.subscription.mapper.SubscriptionMapper;
import com.yowyob.loyalty.infrastructure.persistence.subscription.repository.InvoiceR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class InvoiceRepositoryAdapter implements InvoiceRepository {

    private final InvoiceR2dbcRepository repository;
    private final SubscriptionMapper mapper;

    public InvoiceRepositoryAdapter(InvoiceR2dbcRepository repository, SubscriptionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<InvoiceRecord> save(InvoiceRecord invoice) {
        return repository.save(mapper.toEntity(invoice)).map(mapper::toDomain);
    }

    @Override
    public Flux<InvoiceRecord> findByTenantId(TenantId tenantId) {
        return repository.findAllByTenantId(tenantId.value()).map(mapper::toDomain);
    }

    @Override
    public Flux<InvoiceRecord> findOverduePending(Instant now) {
        return repository.findOverduePending(now).map(mapper::toDomain);
    }
}
