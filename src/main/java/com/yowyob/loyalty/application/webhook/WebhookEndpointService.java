package com.yowyob.loyalty.application.webhook;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.webhook.model.WebhookEndpoint;
import com.yowyob.loyalty.domain.webhook.port.out.WebhookEndpointRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class WebhookEndpointService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final WebhookEndpointRepository repository;

    public WebhookEndpointService(WebhookEndpointRepository repository) {
        this.repository = repository;
    }

    public record CreatedEndpoint(WebhookEndpoint endpoint, String plainSecret) {}

    public Mono<CreatedEndpoint> create(TenantId tenantId, String url, String description, List<String> eventTypes) {
        String secret = generateSecret();
        WebhookEndpoint endpoint = WebhookEndpoint.create(tenantId, url, secret, description, eventTypes);
        return repository.save(endpoint).map(saved -> new CreatedEndpoint(saved, secret));
    }

    public Flux<WebhookEndpoint> listForTenant(TenantId tenantId) {
        return repository.findAllByTenantId(tenantId);
    }

    public Mono<WebhookEndpoint> get(TenantId tenantId, UUID id) {
        return repository.findByIdAndTenantId(id, tenantId);
    }

    public Mono<WebhookEndpoint> update(TenantId tenantId, UUID id, String url, String description, List<String> eventTypes, Boolean active) {
        return repository.findByIdAndTenantId(id, tenantId)
                .map(endpoint -> {
                    WebhookEndpoint updated = endpoint.update(
                            url != null ? url : endpoint.url(),
                            description != null ? description : endpoint.description(),
                            eventTypes != null ? eventTypes : endpoint.eventTypes());
                    if (active != null) {
                        updated = active ? updated.activate() : updated.deactivate();
                    }
                    return updated;
                })
                .flatMap(repository::save);
    }

    public Mono<CreatedEndpoint> rotateSecret(TenantId tenantId, UUID id) {
        String secret = generateSecret();
        return repository.findByIdAndTenantId(id, tenantId)
                .map(endpoint -> endpoint.rotateSecret(secret))
                .flatMap(repository::save)
                .map(saved -> new CreatedEndpoint(saved, secret));
    }

    public Mono<Void> delete(TenantId tenantId, UUID id) {
        return repository.deleteByIdAndTenantId(id, tenantId);
    }

    private static String generateSecret() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return "whsec_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
