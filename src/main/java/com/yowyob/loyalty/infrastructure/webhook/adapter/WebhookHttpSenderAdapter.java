package com.yowyob.loyalty.infrastructure.webhook.adapter;

import com.yowyob.loyalty.domain.webhook.port.out.WebhookSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

@Component
public class WebhookHttpSenderAdapter implements WebhookSenderPort {

    private static final Logger log = LoggerFactory.getLogger(WebhookHttpSenderAdapter.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int MAX_RESPONSE_SNIPPET = 500;

    private final WebClient webClient;

    public WebhookHttpSenderAdapter(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<WebhookAttemptResult> send(String url, String secret, String deliveryId, String eventType, String rawPayloadJson) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String signature = sign(secret, timestamp, rawPayloadJson);

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Webhook-Id", deliveryId)
                .header("X-Webhook-Event", eventType)
                .header("X-Webhook-Timestamp", timestamp)
                .header("X-Webhook-Signature", "sha256=" + signature)
                .bodyValue(rawPayloadJson)
                .exchangeToMono(response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .map(body -> new WebhookAttemptResult(
                                response.statusCode().is2xxSuccessful(),
                                response.statusCode().value(),
                                truncate(body))))
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> {
                    log.warn("Webhook delivery to {} failed: {}", url, e.getMessage());
                    return Mono.just(new WebhookAttemptResult(false, null, truncate(e.getMessage())));
                });
    }

    private static String sign(String secret, String timestamp, String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] signed = mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(signed);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de signer le payload webhook", e);
        }
    }

    private static String truncate(String s) {
        if (s == null) return null;
        return s.length() > MAX_RESPONSE_SNIPPET ? s.substring(0, MAX_RESPONSE_SNIPPET) : s;
    }
}
