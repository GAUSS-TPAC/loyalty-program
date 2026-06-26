package com.yowyob.loyalty.infrastructure.persistence.subscription.repository;

import com.yowyob.loyalty.infrastructure.persistence.subscription.entity.InvoiceRecordEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.UUID;

public interface InvoiceR2dbcRepository extends ReactiveCrudRepository<InvoiceRecordEntity, UUID> {
    Flux<InvoiceRecordEntity> findAllByTenantId(UUID tenantId);

    @Query("SELECT * FROM invoice_records WHERE status = 'PENDING' AND due_date <= :now")
    Flux<InvoiceRecordEntity> findOverduePending(Instant now);
}
