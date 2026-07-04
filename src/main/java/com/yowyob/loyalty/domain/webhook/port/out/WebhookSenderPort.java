package com.yowyob.loyalty.domain.webhook.port.out;

import reactor.core.publisher.Mono;

public interface WebhookSenderPort {

    record WebhookAttemptResult(boolean success, Integer httpStatusCode, String responseSnippet) {}

    Mono<WebhookAttemptResult> send(String url, String secret, String deliveryId, String eventType, String rawPayloadJson);
}
