package com.yowyob.loyalty.domain.subscription.port.out;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.subscription.model.InvoiceRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface InvoiceRepository {
    Mono<InvoiceRecord> save(InvoiceRecord invoice);
    Flux<InvoiceRecord> findByTenantId(TenantId tenantId);
    Flux<InvoiceRecord> findOverduePending(Instant now);
}
