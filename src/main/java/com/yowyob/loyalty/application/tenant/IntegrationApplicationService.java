package com.yowyob.loyalty.application.tenant;

import com.yowyob.loyalty.application.webhook.WebhookEndpointService;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.exception.ApplicationDomainException;
import com.yowyob.loyalty.domain.tenant.exception.ApplicationNotFoundException;
import com.yowyob.loyalty.domain.tenant.exception.InvalidWebhookEventTypeException;
import com.yowyob.loyalty.domain.tenant.model.ApiKey;
import com.yowyob.loyalty.domain.tenant.model.IntegrationApplication;
import com.yowyob.loyalty.domain.tenant.model.enums.ApiKeyMode;
import com.yowyob.loyalty.domain.tenant.port.out.IntegrationApplicationRepository;
import com.yowyob.loyalty.domain.webhook.model.WebhookEventType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Gère les applications d'intégration (modèle « espace marchand » à la My-CoolPay) :
 * une application = clé publique (identifiant exposable) + clé API privée (authentification)
 * + endpoint webhook optionnel (callbacks signés).
 */
@Service
public class IntegrationApplicationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final IntegrationApplicationRepository repository;
    private final ApiKeyService apiKeyService;
    private final WebhookEndpointService webhookEndpointService;

    public IntegrationApplicationService(IntegrationApplicationRepository repository,
                                         ApiKeyService apiKeyService,
                                         WebhookEndpointService webhookEndpointService) {
        this.repository = repository;
        this.apiKeyService = apiKeyService;
        this.webhookEndpointService = webhookEndpointService;
    }

    /** Secrets révélés une seule fois : rawPrivateKey au create/rotate, webhookSecret à la création d'endpoint. */
    public record CreatedApplication(IntegrationApplication app, String rawPrivateKey, String webhookSecret) {}

    /** Vue enrichie pour list/get : préfixe de la clé privée et URL de callback liés. */
    public record ApplicationView(IntegrationApplication app, String keyPrefix, String callbackUrl) {}

    public Mono<CreatedApplication> create(TenantId tenantId, String name, String description,
                                           String websiteUrl, String logoUrl, ApiKeyMode mode,
                                           String callbackUrl, List<String> eventTypes) {
        return Mono.defer(() -> {
            ApiKeyMode effectiveMode = mode != null ? mode : ApiKeyMode.LIVE;
            List<String> validatedTypes = validateEventTypes(eventTypes);
            String publicKey = generatePublicKey(effectiveMode);
            return doCreate(tenantId, name, description, websiteUrl, logoUrl, effectiveMode,
                    callbackUrl, validatedTypes, publicKey);
        });
    }

    private Mono<CreatedApplication> doCreate(TenantId tenantId, String name, String description,
                                              String websiteUrl, String logoUrl, ApiKeyMode effectiveMode,
                                              String callbackUrl, List<String> validatedTypes, String publicKey) {
        return apiKeyService.create(tenantId, "app:" + name, effectiveMode, "sk")
                .flatMap(createdKey -> {
                    if (callbackUrl == null || callbackUrl.isBlank()) {
                        IntegrationApplication app = IntegrationApplication.create(tenantId, name, description,
                                websiteUrl, logoUrl, publicKey, createdKey.record().id(), null, effectiveMode);
                        return repository.save(app)
                                .map(saved -> new CreatedApplication(saved, createdKey.rawKey(), null));
                    }
                    List<String> types = validatedTypes.isEmpty() ? businessEventCodes() : validatedTypes;
                    return webhookEndpointService.create(tenantId, callbackUrl, "Application: " + name, types)
                            .flatMap(createdEndpoint -> {
                                IntegrationApplication app = IntegrationApplication.create(tenantId, name, description,
                                        websiteUrl, logoUrl, publicKey, createdKey.record().id(),
                                        createdEndpoint.endpoint().id(), effectiveMode);
                                return repository.save(app)
                                        .map(saved -> new CreatedApplication(saved, createdKey.rawKey(),
                                                createdEndpoint.plainSecret()));
                            });
                });
    }

    public Flux<ApplicationView> list(TenantId tenantId) {
        return keyPrefixes(tenantId).flatMapMany(prefixes ->
                repository.findAllByTenantId(tenantId)
                        .concatMap(app -> enrich(tenantId, app, prefixes.get(app.apiKeyId()))));
    }

    public Mono<ApplicationView> get(TenantId tenantId, UUID id) {
        return requireApp(tenantId, id)
                .flatMap(app -> keyPrefixes(tenantId)
                        .flatMap(prefixes -> enrich(tenantId, app, prefixes.get(app.apiKeyId()))));
    }

    public Mono<CreatedApplication> update(TenantId tenantId, UUID id, String name, String description,
                                           String websiteUrl, String logoUrl,
                                           String callbackUrl, List<String> eventTypes) {
        return Mono.defer(() -> {
            List<String> validatedTypes = validateEventTypes(eventTypes);
            return doUpdate(tenantId, id, name, description, websiteUrl, logoUrl, callbackUrl, validatedTypes);
        });
    }

    private Mono<CreatedApplication> doUpdate(TenantId tenantId, UUID id, String name, String description,
                                              String websiteUrl, String logoUrl,
                                              String callbackUrl, List<String> validatedTypes) {
        return requireApp(tenantId, id).flatMap(app -> {
            IntegrationApplication updated = app.update(
                    name != null ? name : app.name(),
                    description != null ? description : app.description(),
                    websiteUrl != null ? websiteUrl : app.websiteUrl(),
                    logoUrl != null ? logoUrl : app.logoUrl());
            return applyCallbackChange(tenantId, updated, callbackUrl, validatedTypes);
        });
    }

    public Mono<CreatedApplication> rotatePrivateKey(TenantId tenantId, UUID id) {
        return requireApp(tenantId, id)
                .flatMap(app -> apiKeyService.create(tenantId, "app:" + app.name(), app.mode(), "sk")
                        // créer la nouvelle clé avant de révoquer l'ancienne : api_key_id est NOT NULL
                        .flatMap(createdKey -> repository.save(app.withApiKey(createdKey.record().id()))
                                .flatMap(saved -> apiKeyService.revoke(tenantId, app.apiKeyId())
                                        .thenReturn(new CreatedApplication(saved, createdKey.rawKey(), null)))));
    }

    public Mono<CreatedApplication> rotateWebhookSecret(TenantId tenantId, UUID id) {
        return requireApp(tenantId, id).flatMap(app -> {
            if (app.webhookEndpointId() == null) {
                return Mono.error(new ApplicationDomainException(
                        "L'application n'a pas d'URL de callback configurée"));
            }
            return webhookEndpointService.rotateSecret(tenantId, app.webhookEndpointId())
                    .map(rotated -> new CreatedApplication(app, null, rotated.plainSecret()));
        });
    }

    public Mono<Void> delete(TenantId tenantId, UUID id) {
        return requireApp(tenantId, id)
                .flatMap(app -> apiKeyService.revoke(tenantId, app.apiKeyId())
                        .then(app.webhookEndpointId() != null
                                ? webhookEndpointService.delete(tenantId, app.webhookEndpointId())
                                : Mono.empty())
                        .then(repository.deleteByIdAndTenantId(id, tenantId)));
    }

    public Mono<IntegrationApplication> findByPublicKeyForTenant(TenantId tenantId, String publicKey) {
        return repository.findByPublicKey(publicKey)
                .filter(app -> app.tenantId().equals(tenantId) && app.active());
    }

    private Mono<CreatedApplication> applyCallbackChange(TenantId tenantId, IntegrationApplication app,
                                                         String callbackUrl, List<String> eventTypes) {
        if (callbackUrl == null) {
            // callback non fourni : mise à jour éventuelle des types d'événements seulement
            if (!eventTypes.isEmpty() && app.webhookEndpointId() != null) {
                return webhookEndpointService.update(tenantId, app.webhookEndpointId(), null, null, eventTypes, null)
                        .then(repository.save(app))
                        .map(saved -> new CreatedApplication(saved, null, null));
            }
            return repository.save(app).map(saved -> new CreatedApplication(saved, null, null));
        }
        if (callbackUrl.isBlank()) {
            // chaîne vide explicite : suppression du callback
            Mono<Void> cleanup = app.webhookEndpointId() != null
                    ? webhookEndpointService.delete(tenantId, app.webhookEndpointId())
                    : Mono.empty();
            return cleanup.then(repository.save(app.withoutWebhookEndpoint()))
                    .map(saved -> new CreatedApplication(saved, null, null));
        }
        if (app.webhookEndpointId() != null) {
            return webhookEndpointService.update(tenantId, app.webhookEndpointId(), callbackUrl, null,
                            eventTypes.isEmpty() ? null : eventTypes, null)
                    .then(repository.save(app))
                    .map(saved -> new CreatedApplication(saved, null, null));
        }
        List<String> types = eventTypes.isEmpty() ? businessEventCodes() : eventTypes;
        return webhookEndpointService.create(tenantId, callbackUrl, "Application: " + app.name(), types)
                .flatMap(created -> repository.save(app.withWebhookEndpoint(created.endpoint().id()))
                        .map(saved -> new CreatedApplication(saved, null, created.plainSecret())));
    }

    private Mono<IntegrationApplication> requireApp(TenantId tenantId, UUID id) {
        return repository.findByIdAndTenantId(id, tenantId)
                .switchIfEmpty(Mono.error(new ApplicationNotFoundException(id)));
    }

    private Mono<Map<UUID, String>> keyPrefixes(TenantId tenantId) {
        return apiKeyService.listForTenant(tenantId).collectMap(ApiKey::id, ApiKey::keyPrefix);
    }

    private Mono<ApplicationView> enrich(TenantId tenantId, IntegrationApplication app, String keyPrefix) {
        if (app.webhookEndpointId() == null) {
            return Mono.just(new ApplicationView(app, keyPrefix, null));
        }
        return webhookEndpointService.get(tenantId, app.webhookEndpointId())
                .map(endpoint -> new ApplicationView(app, keyPrefix, endpoint.url()))
                .defaultIfEmpty(new ApplicationView(app, keyPrefix, null));
    }

    private static List<String> validateEventTypes(List<String> eventTypes) {
        if (eventTypes == null) return List.of();
        return eventTypes.stream().map(code -> {
            try {
                return WebhookEventType.fromCode(code).code();
            } catch (IllegalArgumentException e) {
                throw new InvalidWebhookEventTypeException(code);
            }
        }).toList();
    }

    private static List<String> businessEventCodes() {
        return Arrays.stream(WebhookEventType.values())
                .filter(type -> type != WebhookEventType.WEBHOOK_TEST)
                .map(WebhookEventType::code)
                .toList();
    }

    private static String generatePublicKey(ApiKeyMode mode) {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        String prefix = mode == ApiKeyMode.TEST ? "pk_test_" : "pk_live_";
        return prefix + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
