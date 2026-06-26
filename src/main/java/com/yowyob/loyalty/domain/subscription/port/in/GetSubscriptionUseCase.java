package com.yowyob.loyalty.domain.subscription.port.in;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.subscription.model.InvoiceRecord;
import com.yowyob.loyalty.domain.subscription.model.TenantSubscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetSubscriptionUseCase {
    Mono<TenantSubscription> getSubscription(TenantId tenantId);
    Mono<TenantSubscription> getSubscriptionById(UUID id);
    Flux<InvoiceRecord> getInvoices(TenantId tenantId);
}
