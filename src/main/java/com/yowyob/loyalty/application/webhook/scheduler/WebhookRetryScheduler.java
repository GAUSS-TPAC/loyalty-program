package com.yowyob.loyalty.application.webhook.scheduler;

import com.yowyob.loyalty.domain.webhook.model.DeliveryStatus;
import com.yowyob.loyalty.domain.webhook.model.WebhookDelivery;
import com.yowyob.loyalty.domain.webhook.port.out.WebhookDeliveryRepository;
import com.yowyob.loyalty.domain.webhook.port.out.WebhookEndpointRepository;
import com.yowyob.loyalty.domain.webhook.port.out.WebhookSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Mono;

import java.time.Instant;

public class WebhookRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(WebhookRetryScheduler.class);

    private final WebhookDeliveryRepository deliveryRepository;
    private final WebhookEndpointRepository endpointRepository;
    private final WebhookSenderPort senderPort;

    public WebhookRetryScheduler(WebhookDeliveryRepository deliveryRepository,
                                  WebhookEndpointRepository endpointRepository,
                                  WebhookSenderPort senderPort) {
        this.deliveryRepository = deliveryRepository;
        this.endpointRepository = endpointRepository;
        this.senderPort = senderPort;
    }

    @Scheduled(cron = "${loyalty.webhook.retry.cron:0 */2 * * * *}")
    public void retryDueDeliveries() {
        deliveryRepository.findDueForRetry(DeliveryStatus.FAILED, Instant.now())
                .flatMap(this::retry)
                .doOnError(e -> log.error("Webhook retry sweep error: {}", e.getMessage()))
                .subscribe();
    }

    private Mono<Void> retry(WebhookDelivery delivery) {
        return endpointRepository.findByIdAndTenantId(delivery.endpointId(), delivery.tenantId())
                .flatMap(endpoint -> senderPort.send(endpoint.url(), endpoint.secret(),
                                delivery.id().toString(), delivery.eventType(), delivery.payload())
                        .flatMap(result -> {
                            WebhookDelivery updated = result.success()
                                    ? delivery.markSucceeded(result.httpStatusCode(), result.responseSnippet())
                                    : delivery.markFailed(result.httpStatusCode(), result.responseSnippet());
                            return deliveryRepository.save(updated);
                        })
                        .onErrorResume(e -> deliveryRepository.save(delivery.markFailed(null, e.getMessage()))))
                .then()
                .onErrorResume(e -> {
                    log.warn("Could not retry webhook delivery {}: {}", delivery.id(), e.getMessage());
                    return Mono.empty();
                });
    }
}
