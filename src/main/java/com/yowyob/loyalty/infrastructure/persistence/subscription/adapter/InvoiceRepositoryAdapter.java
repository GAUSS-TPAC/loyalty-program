package com.yowyob.loyalty.infrastructure.persistence.subscription.adapter;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.subscription.model.InvoiceRecord;
import com.yowyob.loyalty.domain.subscription.port.out.InvoiceRepository;
import com.yowyob.loyalty.infrastructure.persistence.subscription.entity.InvoiceRecordEntity;
import com.yowyob.loyalty.infrastructure.persistence.subscription.mapper.SubscriptionMapper;
import com.yowyob.loyalty.infrastructure.persistence.subscription.repository.InvoiceR2dbcRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class InvoiceRepositoryAdapter implements InvoiceRepository {

    private final InvoiceR2dbcRepository repository;
    private final SubscriptionMapper mapper;
    private final R2dbcEntityTemplate template;

    public InvoiceRepositoryAdapter(InvoiceR2dbcRepository repository, SubscriptionMapper mapper, R2dbcEntityTemplate template) {
        this.repository = repository;
        this.mapper = mapper;
        this.template = template;
    }

    // Client-generated UUID id + no Persistable => save() always issues UPDATE. Decide
    // insert vs update explicitly (see RuleRepositoryAdapter for the full explanation).
    @Override
    public Mono<InvoiceRecord> save(InvoiceRecord invoice) {
        InvoiceRecordEntity entity = mapper.toEntity(invoice);
        return repository.existsById(entity.getId())
                .flatMap(exists -> exists ? repository.save(entity) : template.insert(entity))
                .map(mapper::toDomain);
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
