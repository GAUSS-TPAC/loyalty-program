package com.yowyob.loyaulty.program.api.webhook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Reçoit les callbacks entrants des providers de paiement (MTN, Orange, Stripe).
 * Ces endpoints sont PUBLICS — aucun JWT requis.
 */
@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    @PostMapping("/mtn")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> handleMtn(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-Callback-Url", required = false) String callbackUrl) {
        log.info("MTN webhook received: transactionId={}", payload.get("transactionId"));
        return Mono.empty();
    }

    @PostMapping("/orange")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> handleOrange(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-Notification-Token", required = false) String token) {
        log.info("Orange webhook received: notifToken={}", token);
        return Mono.empty();
    }

    @PostMapping("/stripe")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> handleStripe(
            @RequestBody String rawPayload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signature) {
        log.info("Stripe webhook received, signaturePresent={}", signature != null);
        return Mono.empty();
    }

    @GetMapping("/health")
    public Mono<Map<String, String>> health() {
        return Mono.just(Map.of("status", "UP", "service", "webhook-receiver"));
    }
}
