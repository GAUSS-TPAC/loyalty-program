package com.yowyob.loyalty.domain.webhook.port.out;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.webhook.model.DeliveryStatus;
import com.yowyob.loyalty.domain.webhook.model.WebhookDelivery;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface WebhookDeliveryRepository {
    Mono<WebhookDelivery> save(WebhookDelivery delivery);
    Flux<WebhookDelivery> findAllByTenantId(TenantId tenantId, int page, int size);
    Flux<WebhookDelivery> findDueForRetry(DeliveryStatus status, Instant now);
}
