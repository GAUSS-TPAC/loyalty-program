package com.yowyob.loyalty.application.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.IntegrationApplication;
import com.yowyob.loyalty.domain.tenant.port.out.IntegrationApplicationRepository;
import com.yowyob.loyalty.domain.webhook.model.WebhookDelivery;
import com.yowyob.loyalty.domain.webhook.model.WebhookEndpoint;
import com.yowyob.loyalty.domain.webhook.model.WebhookEventType;
import com.yowyob.loyalty.domain.webhook.port.out.WebhookDeliveryRepository;
import com.yowyob.loyalty.domain.webhook.port.out.WebhookEndpointRepository;
import com.yowyob.loyalty.domain.webhook.port.out.WebhookSenderPort;
import com.yowyob.loyalty.domain.webhook.port.out.WebhookSenderPort.WebhookAttemptResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WebhookDispatchService {

    private static final Logger log = LoggerFactory.getLogger(WebhookDispatchService.class);

    private final WebhookEndpointRepository endpointRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final WebhookSenderPort senderPort;
    private final ObjectMapper objectMapper;
    private final IntegrationApplicationRepository applicationRepository;

    public WebhookDispatchService(WebhookEndpointRepository endpointRepository,
                                   WebhookDeliveryRepository deliveryRepository,
                                   WebhookSenderPort senderPort,
                                   ObjectMapper objectMapper,
                                   IntegrationApplicationRepository applicationRepository) {
        this.endpointRepository = endpointRepository;
        this.deliveryRepository = deliveryRepository;
        this.senderPort = senderPort;
        this.objectMapper = objectMapper;
        this.applicationRepository = applicationRepository;
    }

    public Mono<Void> dispatch(TenantId tenantId, WebhookEventType eventType, Map<String, Object> data) {
        return endpointRepository.findActiveByTenantId(tenantId)
                .filter(endpoint -> endpoint.isSubscribedTo(eventType.code()))
                .flatMap(endpoint -> dispatchToEndpoint(tenantId, endpoint, eventType.code(), data))
                .then()
                .onErrorResume(e -> {
                    log.error("Webhook dispatch failed for tenant {} event {}: {}", tenantId, eventType.code(), e.getMessage());
                    return Mono.empty();
                });
    }

    public Flux<WebhookDelivery> listDeliveries(TenantId tenantId, int page, int size) {
        return deliveryRepository.findAllByTenantId(tenantId, page, size);
    }

    public Mono<WebhookAttemptResult> sendTestPing(TenantId tenantId, UUID endpointId) {
        return endpointRepository.findByIdAndTenantId(endpointId, tenantId)
                .flatMap(endpoint -> applicationPublicKey(endpoint.id())
                        .flatMap(publicKey -> {
                            String payload = buildEnvelope(WebhookEventType.WEBHOOK_TEST.code(),
                                    Map.of("message", "This is a test webhook delivery"), publicKey);
                            WebhookDelivery pending = WebhookDelivery.createPending(tenantId, endpoint.id(),
                                    WebhookEventType.WEBHOOK_TEST.code(), payload);
                            return deliveryRepository.save(pending)
                                    .flatMap(saved -> attempt(endpoint, saved, payload));
                        }));
    }

    private Mono<Void> dispatchToEndpoint(TenantId tenantId, WebhookEndpoint endpoint, String eventTypeCode, Map<String, Object> data) {
        return applicationPublicKey(endpoint.id())
                .flatMap(publicKey -> {
                    String payload = buildEnvelope(eventTypeCode, data, publicKey);
                    WebhookDelivery pending = WebhookDelivery.createPending(tenantId, endpoint.id(), eventTypeCode, payload);
                    return deliveryRepository.save(pending)
                            .flatMap(saved -> attempt(endpoint, saved, payload));
                })
                .then();
    }

    /** Clé publique de l'application liée à l'endpoint (champ "application" du callback), vide si endpoint autonome. */
    private Mono<String> applicationPublicKey(UUID endpointId) {
        return applicationRepository.findByWebhookEndpointId(endpointId)
                .map(IntegrationApplication::publicKey)
                .defaultIfEmpty("");
    }

    private Mono<WebhookAttemptResult> attempt(WebhookEndpoint endpoint, WebhookDelivery delivery, String payload) {
        return senderPort.send(endpoint.url(), endpoint.secret(), delivery.id().toString(), delivery.eventType(), payload)
                .flatMap(result -> {
                    WebhookDelivery updated = result.success()
                            ? delivery.markSucceeded(result.httpStatusCode(), result.responseSnippet())
                            : delivery.markFailed(result.httpStatusCode(), result.responseSnippet());
                    return deliveryRepository.save(updated).thenReturn(result);
                })
                .onErrorResume(e -> {
                    log.warn("Webhook send error to {}: {}", endpoint.url(), e.getMessage());
                    WebhookAttemptResult failure = new WebhookAttemptResult(false, null, e.getMessage());
                    return deliveryRepository.save(delivery.markFailed(null, e.getMessage())).thenReturn(failure);
                });
    }

    private String buildEnvelope(String eventTypeCode, Map<String, Object> data, String applicationPublicKey) {
        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("id", UUID.randomUUID().toString());
            envelope.put("type", eventTypeCode);
            envelope.put("createdAt", Instant.now().toString());
            if (applicationPublicKey != null && !applicationPublicKey.isBlank()) {
                envelope.put("application", applicationPublicKey);
            }
            envelope.put("data", data);
            return objectMapper.writeValueAsString(envelope);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de sérialiser le payload webhook", e);
        }
    }
}
